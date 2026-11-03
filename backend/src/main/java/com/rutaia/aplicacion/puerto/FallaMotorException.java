package com.rutaia.aplicacion.puerto;

/**
 * El motor no respondió (conexión rechazada o timeout), respondió con error o devolvió algo que no cumple el contrato
 * de {@link MotorRecomendacion}. {@code ConsultaServicioImpl} la convierte en el estado Error; el mensaje es el motivo
 * técnico que va a detalle_error y a los logs. Nunca llega al manejador global de excepciones.
 */
public class FallaMotorException extends RuntimeException {

	public FallaMotorException(String motivo) {
		super(motivo);
	}

	public FallaMotorException(String motivo, Throwable causa) {
		super(motivo, causa);
	}

}
