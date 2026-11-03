package com.rutaia.integracion.n8n;

import com.rutaia.dominio.NivelExperiencia;

/** Cuerpo que Spring Boot envía al flujo W-1 «RutaIA - Consulta RAG» (C8, RF 08). */
record N8nPeticion(
		Long consultaId,
		String pregunta,
		NivelExperiencia nivelExperiencia,
		String areaInteres) {
}
