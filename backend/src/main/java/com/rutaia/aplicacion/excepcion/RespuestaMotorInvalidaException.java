package com.rutaia.aplicacion.excepcion;

/**
 * n8n respondió 2xx, pero el contenido no cumple C8 (consultaId distinto, estado Error o inválido, respuesta vacía,
 * fuentes fuera de regla o cursos inexistentes o inactivos en PostgreSQL). La consulta termina en Error.
 */
public class RespuestaMotorInvalidaException extends RuntimeException {

	public RespuestaMotorInvalidaException(String motivo) {
		super(motivo);
	}

}
