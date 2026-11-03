package com.rutaia.aplicacion.excepcion;

/** Se traduce a 404. */
public class RecursoNoEncontradoException extends RuntimeException {

	public RecursoNoEncontradoException(String mensaje) {
		super(mensaje);
	}

}
