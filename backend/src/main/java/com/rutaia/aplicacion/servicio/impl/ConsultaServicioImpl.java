package com.rutaia.aplicacion.servicio.impl;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.rutaia.aplicacion.dto.consulta.ConsultaRequest;
import com.rutaia.aplicacion.dto.consulta.ConsultaResultadoResponse;
import com.rutaia.aplicacion.excepcion.RespuestaMotorInvalidaException;
import com.rutaia.aplicacion.puerto.FallaMotorException;
import com.rutaia.aplicacion.puerto.MotorRecomendacion;
import com.rutaia.aplicacion.puerto.ResultadoRecomendacion;
import com.rutaia.aplicacion.puerto.SolicitudRecomendacion;
import com.rutaia.aplicacion.servicio.ConsultaServicio;

/**
 * E-11 (POST /api/consultas). Sin @Transactional a propósito: la consulta se confirma en Pendiente antes de llamar al
 * motor, la llamada corre fuera de toda transacción y el estado final se guarda en una transacción aparte
 * ({@link ConsultaTransacciones}). Ninguna petición termina con la consulta en Pendiente. Depende solo del puerto
 * {@link MotorRecomendacion}: no sabe qué motor lo implementa.
 */
@Service
public class ConsultaServicioImpl implements ConsultaServicio {

	private static final Logger log = LoggerFactory.getLogger(ConsultaServicioImpl.class);

	private static final int MAX_DETALLE_ERROR = 1000;

	private final ConsultaTransacciones transacciones;
	private final MotorRecomendacion motorRecomendacion;

	public ConsultaServicioImpl(ConsultaTransacciones transacciones, MotorRecomendacion motorRecomendacion) {
		this.transacciones = transacciones;
		this.motorRecomendacion = motorRecomendacion;
	}

	@Override
	public ConsultaResultadoResponse registrar(ConsultaRequest solicitud) {
		SolicitudRecomendacion solicitudMotor = transacciones.registrarPendiente(solicitud);
		Long consultaId = solicitudMotor.consultaId();

		long inicio = System.nanoTime();
		ResultadoRecomendacion resultadoMotor;
		try {
			resultadoMotor = motorRecomendacion.recomendar(solicitudMotor);
		}
		catch (FallaMotorException ex) {
			return finalizarConError(consultaId, ex.getMessage(), milisegundosDesde(inicio));
		}
		long duracionMotor = milisegundosDesde(inicio);

		try {
			ConsultaResultadoResponse resultado = resultadoMotor.esRespondida()
					? transacciones.guardarRespondida(consultaId, resultadoMotor)
					: transacciones.guardarSinResultados(consultaId);
			log.info("Consulta {} finalizada: estado={}, motor={} ms, fuentes={}", consultaId,
					resultado.estado().getTexto(), duracionMotor, resultado.fuentes().size());
			return resultado;
		}
		catch (RespuestaMotorInvalidaException ex) {
			return finalizarConError(consultaId, ex.getMessage(), duracionMotor);
		}
		catch (RuntimeException ex) {
			// Falla al guardar el resultado (por ejemplo, de la BD): la consulta no debe quedar en Pendiente.
			log.error("Consulta {}: error inesperado al guardar el resultado del motor", consultaId, ex);
			return finalizarConError(consultaId,
					"Error inesperado al guardar el resultado: " + ex.getClass().getSimpleName(), duracionMotor);
		}
	}

	private ConsultaResultadoResponse finalizarConError(Long consultaId, String motivo, long duracionMotor) {
		String detalle = motivo.length() <= MAX_DETALLE_ERROR ? motivo : motivo.substring(0, MAX_DETALLE_ERROR) + "…";
		ConsultaResultadoResponse resultado = transacciones.guardarError(consultaId, detalle);
		log.warn("Consulta {} finalizada: estado={}, motor={} ms, motivo={}", consultaId,
				resultado.estado().getTexto(), duracionMotor, detalle);
		return resultado;
	}

	private static long milisegundosDesde(long inicioNanos) {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicioNanos);
	}

}
