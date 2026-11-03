package com.rutaia.aplicacion.puerto;

import com.rutaia.dominio.NivelExperiencia;

/** Lo que el motor necesita para recomendar: la consulta guardada y el perfil del estudiante (RF 08). */
public record SolicitudRecomendacion(
		Long consultaId,
		String pregunta,
		NivelExperiencia nivelExperiencia,
		String areaInteres) {
}
