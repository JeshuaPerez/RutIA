package com.rutaia.integracion.n8n;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Cuerpo que devuelve W-1 (C8). Los campos se leen sin validar: estado y nivel quedan como texto para que un valor
 * fuera de C5 no impida leer el resto y se trate como respuesta inválida al validarla.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record N8nRespuesta(
		Long consultaId,
		String pregunta,
		String estado,
		String respuesta,
		List<Fuente> fuentes,
		String error) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Fuente(
			Long cursoId,
			String nombre,
			String descripcion,
			String categoria,
			String nivel,
			Integer duracionHoras,
			Double similitud) {
	}

}
