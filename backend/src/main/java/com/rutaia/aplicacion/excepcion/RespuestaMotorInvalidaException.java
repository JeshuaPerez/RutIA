package com.rutaia.aplicacion.excepcion;

/**
 * El motor devolvió un resultado que cumple su contrato, pero alguna fuente apunta a un curso que no existe o está
 * inactivo en PostgreSQL. No se escribe la recomendación y la consulta termina en Error.
 */
public class RespuestaMotorInvalidaException extends RuntimeException {

	public RespuestaMotorInvalidaException(String motivo) {
		super(motivo);
	}

}
