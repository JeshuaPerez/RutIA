package com.rutaia.servicio;

/**
 * n8n respondió 2xx, pero el contenido no cumple C8 (consultaId distinto, estado Error o inválido, respuesta vacía,
 * fuentes fuera de regla o cursos inexistentes o inactivos en PostgreSQL). La consulta termina en Error.
 */
class RespuestaN8nInvalidaException extends RuntimeException {

	RespuestaN8nInvalidaException(String motivo) {
		super(motivo);
	}

}
