package com.rutaia.integracion;

import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cliente del flujo W-1 «RutaIA - Consulta RAG» (C8). Spring Boot es el único que lo llama (RF 06 y RF 08). No
 * reintenta: cualquier falla de transporte o de HTTP se informa con {@link FallaN8nException}.
 */
@Component
public class ClienteN8n {

	private static final Duration TIMEOUT_CONEXION = Duration.ofSeconds(5);
	private static final int MAX_CUERPO_EN_MOTIVO = 300;

	private final RestClient restClient;
	private final JsonMapper jsonMapper;
	private final Duration timeoutLectura;

	public ClienteN8n(@Value("${rutaia.n8n.webhook-url}") String webhookUrl,
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

	/**
	 * Envía la consulta a W-1 y devuelve su cuerpo 2xx sin validar el contenido (eso lo hace el servicio).
	 *
	 * @throws FallaN8nException si n8n no responde, responde no-2xx o el cuerpo no es un JSON legible
	 */
	public N8nRespuesta consultar(N8nPeticion peticion) {
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
							throw new FallaN8nException("n8n respondió HTTP " + status.value() + ": " + detalle);
						}
						if (leida == null) {
							throw new FallaN8nException("n8n respondió HTTP " + status.value()
									+ " con un cuerpo ilegible: " + recortar(cuerpo));
						}
						return leida;
					});
		}
		catch (RestClientException ex) {
			throw new FallaN8nException(describirFallaDeConexion(ex), ex);
		}
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

	private static String recortar(String cuerpo) {
		if (cuerpo.isBlank()) {
			return "(vacío)";
		}
		return cuerpo.length() <= MAX_CUERPO_EN_MOTIVO ? cuerpo : cuerpo.substring(0, MAX_CUERPO_EN_MOTIVO) + "…";
	}

}
