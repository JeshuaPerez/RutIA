package com.rutaia.servicio;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rutaia.dto.ConsultaRequest;
import com.rutaia.dto.ConsultaResultado;
import com.rutaia.entidad.EstadoConsulta;
import com.rutaia.integracion.ClienteN8n;
import com.rutaia.integracion.FallaN8nException;
import com.rutaia.integracion.N8nPeticion;
import com.rutaia.integracion.N8nRespuesta;

/**
 * E-11 (POST /api/consultas). Sin @Transactional a propósito: la consulta se confirma en Pendiente antes de llamar a
 * n8n, la llamada corre fuera de toda transacción y el estado final se guarda en una transacción aparte
 * ({@link ConsultaTransacciones}). Ninguna petición termina con la consulta en Pendiente.
 */
@Service
public class ConsultaServicio {

	private static final Logger log = LoggerFactory.getLogger(ConsultaServicio.class);

	private static final int MAX_FUENTES = 5;
	private static final int MAX_DETALLE_ERROR = 1000;

	private final ConsultaTransacciones transacciones;
	private final ClienteN8n clienteN8n;

	public ConsultaServicio(ConsultaTransacciones transacciones, ClienteN8n clienteN8n) {
		this.transacciones = transacciones;
		this.clienteN8n = clienteN8n;
	}

	public ConsultaResultado registrar(ConsultaRequest solicitud) {
		N8nPeticion peticion = transacciones.registrarPendiente(solicitud);
		Long consultaId = peticion.consultaId();

		long inicio = System.nanoTime();
		N8nRespuesta respuesta;
		try {
			respuesta = clienteN8n.consultar(peticion);
		}
		catch (FallaN8nException ex) {
			return finalizarConError(consultaId, ex.getMessage(), milisegundosDesde(inicio));
		}
		long duracionN8n = milisegundosDesde(inicio);

		try {
			validar(respuesta, consultaId);
			ConsultaResultado resultado = EstadoConsulta.SIN_RESULTADOS.getTexto().equals(respuesta.estado())
					? transacciones.guardarSinResultados(consultaId)
					: transacciones.guardarRespondida(consultaId, respuesta);
			log.info("Consulta {} finalizada: estado={}, n8n={} ms, fuentes={}", consultaId,
					resultado.estado().getTexto(), duracionN8n, resultado.fuentes().size());
			return resultado;
		}
		catch (RespuestaN8nInvalidaException ex) {
			return finalizarConError(consultaId, ex.getMessage(), duracionN8n);
		}
		catch (RuntimeException ex) {
			// Falla al guardar el resultado (por ejemplo, de la BD): la consulta no debe quedar en Pendiente.
			log.error("Consulta {}: error inesperado al guardar el resultado de n8n", consultaId, ex);
			return finalizarConError(consultaId,
					"Error inesperado al guardar el resultado: " + ex.getClass().getSimpleName(), duracionN8n);
		}
	}

	private ConsultaResultado finalizarConError(Long consultaId, String motivo, long duracionN8n) {
		String detalle = motivo.length() <= MAX_DETALLE_ERROR ? motivo : motivo.substring(0, MAX_DETALLE_ERROR) + "…";
		ConsultaResultado resultado = transacciones.guardarError(consultaId, detalle);
		log.warn("Consulta {} finalizada: estado={}, n8n={} ms, motivo={}", consultaId,
				resultado.estado().getTexto(), duracionN8n, detalle);
		return resultado;
	}

	/**
	 * Reglas de C8 que no dependen de la BD. Una respuesta válida trae el mismo consultaId y estado Respondida o Sin
	 * resultados; si es Respondida, además una respuesta no vacía y de 1 a 5 fuentes sin cursoId repetido y con
	 * similitud entre 0 y 1. Que cada curso exista y esté activo se comprueba al guardar.
	 */
	private static void validar(N8nRespuesta respuesta, Long consultaId) {
		if (!consultaId.equals(respuesta.consultaId())) {
			throw invalida("n8n devolvió consultaId " + respuesta.consultaId() + ", distinto del enviado " + consultaId);
		}
		String estado = respuesta.estado();
		if (EstadoConsulta.ERROR.getTexto().equals(estado)) {
			throw invalida("n8n devolvió estado Error: " + respuesta.error());
		}
		if (EstadoConsulta.SIN_RESULTADOS.getTexto().equals(estado)) {
			return;
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
	}

	private static RespuestaN8nInvalidaException invalida(String motivo) {
		return new RespuestaN8nInvalidaException(motivo);
	}

	private static long milisegundosDesde(long inicioNanos) {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNanos);
	}

}
