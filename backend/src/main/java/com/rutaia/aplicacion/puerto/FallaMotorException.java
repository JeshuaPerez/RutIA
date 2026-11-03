package com.rutaia.aplicacion.puerto;

/**
 * n8n no respondió (conexión rechazada o timeout), respondió no-2xx o con un cuerpo ilegible. El servicio la
 * convierte en el estado Error; el mensaje es el motivo técnico que va a detalle_error y a los logs.
 */
public class FallaMotorException extends RuntimeException {

	public FallaMotorException(String motivo) {
		super(motivo);
	}

	public FallaMotorException(String motivo, Throwable causa) {
		super(motivo, causa);
	}

}
