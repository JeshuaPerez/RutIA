package com.rutaia.aplicacion.excepcion;

/** Se traduce a 409. */
public class ConflictoException extends RuntimeException {

	private final String campo;

	public ConflictoException(String mensaje, String campo) {
		super(mensaje);
		this.campo = campo;
	}

	public String getCampo() {
		return campo;
	}

}
