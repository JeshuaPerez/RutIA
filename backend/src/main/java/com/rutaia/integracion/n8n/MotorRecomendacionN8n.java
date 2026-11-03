package com.rutaia.integracion.n8n;

import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.rutaia.aplicacion.puerto.FallaMotorException;
import com.rutaia.aplicacion.puerto.MotorRecomendacion;
import com.rutaia.aplicacion.puerto.ResultadoRecomendacion;
import com.rutaia.aplicacion.puerto.SolicitudRecomendacion;
import com.rutaia.dominio.EstadoConsulta;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptador del flujo W-1 «RutaIA - Consulta RAG» (C8) al puerto {@link MotorRecomendacion} (patrón Adapter). Es la
 * única clase que conoce n8n: la URL del webhook, su JSON, sus códigos HTTP y los timeouts. Spring Boot es el único
 * que llama a W-1 (RF 06 y RF 08). No reintenta: toda falla de transporte, de HTTP o de contenido se informa con
 * {@link FallaMotorException}.
 */
@Component
public class MotorRecomendacionN8n implements MotorRecomendacion {

	private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(5);
	private static final int MAX_CUERPO_EN_MOTIVO = 300;
	private static final int MAX_FUENTES = 5;

	private final RestClient restClient;
	private final JsonMapper jsonMapper;
	private final Duration timeoutLectura;

	public MotorRecomendacionN8n(@Value("${rutaia.n8n.webhook-url}") String webhookUrl,
			@Value("${rutaia.n8n.timeout-segundos}") long timeoutSegundos, JsonMapper jsonMapper) {
		this.timeoutLectura = Duration.ofSeconds(timeoutSegundos);
		// HTTP/1.1 explícito: con http:// el cliente del JDK intentaría antes una actualización a HTTP/2.
		HttpClient httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(TIMEOUT_CONEXION)
				.build();
		JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(httpClient);
		fabrica.setReadTimeout(timeoutLectura);
		this.restClient = RestClient.builder()
				.baseUrl(webhookUrl)
				.requestFactory(fabrica)
				.build();
		this.jsonMapper = jsonMapper;
	}

	@Override
	public ResultadoRecomendacion recomendar(SolicitudRecomendacion solicitud) {
		N8nPeticion peticion = new N8nPeticion(solicitud.consultaId(), solicitud.pregunta(),
				solicitud.nivelExperiencia(), solicitud.areaInteres());
		return traducir(consultar(peticion), solicitud.consultaId());
	}

	/**
	 * Envía la consulta a W-1 y devuelve su cuerpo 2xx sin validar el contenido.
	 *
	 * @throws FallaMotorException si n8n no responde, responde no-2xx o el cuerpo no es un JSON legible
	 */
	private N8nRespuesta consultar(N8nPeticion peticion) {
		try {
			return restClient.post()
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.body(peticion)
					.exchange((solicitud, respuesta) -> {
						HttpStatusCode status = respuesta.getStatusCode();
						String cuerpo = new String(respuesta.getBody().readAllBytes(), StandardCharsets.UTF_8);
						N8nRespuesta leida = leer(cuerpo);
						if (!status.is2xxSuccessful()) {
							String detalle = leida != null && leida.error() != null ? leida.error() : recortar(cuerpo);
							throw new FallaMotorException("n8n respondió HTTP " + status.value() + ": " + detalle);
						}
						if (leida == null) {
							throw new FallaMotorException("n8n respondió HTTP " + status.value()
									+ " con un cuerpo ilegible: " + recortar(cuerpo));
						}
						return leida;
					});
		}
		catch (RestClientException ex) {
			throw new FallaMotorException(describirFallaDeConexion(ex), ex);
		}
	}

	/**
	 * Reglas de C8 que no dependen de la BD. Una respuesta válida trae el mismo consultaId y estado Respondida o Sin
	 * resultados; si es Respondida, además una respuesta no vacía y de 1 a 5 fuentes sin cursoId repetido y con
	 * similitud entre 0 y 1. Que cada curso exista y esté activo lo comprueba la aplicación al guardar.
	 */
	private static ResultadoRecomendacion traducir(N8nRespuesta respuesta, Long consultaId) {
		if (!consultaId.equals(respuesta.consultaId())) {
			throw invalida("n8n devolvió consultaId " + respuesta.consultaId() + ", distinto del enviado " + consultaId);
		}
		String estado = respuesta.estado();
		if (EstadoConsulta.ERROR.getTexto().equals(estado)) {
			throw invalida("n8n devolvió estado Error: " + respuesta.error());
		}
		if (EstadoConsulta.SIN_RESULTADOS.getTexto().equals(estado)) {
			return ResultadoRecomendacion.sinResultados();
		}
		if (!EstadoConsulta.RESPONDIDA.getTexto().equals(estado)) {
			throw invalida("n8n devolvió un estado inválido: " + estado);
		}
		if (respuesta.respuesta() == null || respuesta.respuesta().isBlank()) {
			throw invalida("n8n devolvió Respondida con la respuesta vacía");
		}
		int cantidad = respuesta.fuentes() == null ? 0 : respuesta.fuentes().size();
		if (cantidad < 1 || cantidad > MAX_FUENTES) {
			throw invalida("n8n devolvió Respondida con " + cantidad + " fuentes; se esperan de 1 a " + MAX_FUENTES);
		}
		Set<Long> cursoIds = new HashSet<>();
		for (N8nRespuesta.Fuente fuente : respuesta.fuentes()) {
			if (fuente == null || fuente.cursoId() == null) {
				throw invalida("n8n devolvió una fuente sin cursoId");
			}
			if (!cursoIds.add(fuente.cursoId())) {
				throw invalida("n8n devolvió el cursoId " + fuente.cursoId() + " repetido en las fuentes");
			}
			Double similitud = fuente.similitud();
			if (similitud == null || similitud.isNaN() || similitud < 0 || similitud > 1) {
				throw invalida("n8n devolvió la similitud " + Objects.toString(similitud) + " para el cursoId "
						+ fuente.cursoId() + "; debe estar entre 0 y 1");
			}
		}
		List<ResultadoRecomendacion.Fuente> fuentes = respuesta.fuentes().stream()
				.map(fuente -> new ResultadoRecomendacion.Fuente(fuente.cursoId(), fuente.similitud()))
				.toList();
		return ResultadoRecomendacion.respondida(respuesta.respuesta(), fuentes);
	}

	/** null si el cuerpo está vacío o no es un JSON con la forma de C8. */
	private N8nRespuesta leer(String cuerpo) {
		if (cuerpo.isBlank()) {
			return null;
		}
		try {
			return jsonMapper.readValue(cuerpo, N8nRespuesta.class);
		}
		catch (JacksonException ex) {
			return null;
		}
	}

	private String describirFallaDeConexion(RestClientException ex) {
		for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
			if (causa instanceof HttpConnectTimeoutException) {
				return "n8n no respondió: timeout de conexión (" + TIMEOUT_CONEXION.toSeconds() + " s)";
			}
			if (causa instanceof HttpTimeoutException) {
				return "n8n no respondió: timeout de lectura (" + timeoutLectura.toSeconds() + " s)";
			}
			if (causa instanceof ConnectException) {
				return "n8n no respondió: conexión rechazada";
			}
		}
		return "n8n no respondió: " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
	}

	private static FallaMotorException invalida(String motivo) {
		return new FallaMotorException(motivo);
	}

	private static String recortar(String cuerpo) {
		if (cuerpo.isBlank()) {
			return "(vacío)";
		}
		return cuerpo.length() <= MAX_CUERPO_EN_MOTIVO ? cuerpo : cuerpo.substring(0, MAX_CUERPO_EN_MOTIVO) + "…";
	}

}
