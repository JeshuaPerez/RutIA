package com.rutaia.integracion;

import com.rutaia.entidad.NivelExperiencia;

/** Cuerpo que Spring Boot envía al flujo W-1 «RutaIA - Consulta RAG» (C8, RF 08). */
public record N8nPeticion(
		Long consultaId,
		String pregunta,
		NivelExperiencia nivelExperiencia,
		String areaInteres) {
}
