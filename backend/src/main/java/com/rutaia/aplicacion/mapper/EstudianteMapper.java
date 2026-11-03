package com.rutaia.aplicacion.mapper;

import com.rutaia.aplicacion.dto.estudiante.EstudianteRequest;
import com.rutaia.aplicacion.dto.estudiante.EstudianteResponse;
import com.rutaia.dominio.Estudiante;

/** Conversión entre la entidad Estudiante y sus DTO. */
public final class EstudianteMapper {

	private EstudianteMapper() {
	}

	/** El DTO ya entrega el correo con trim y en minúsculas. */
	public static Estudiante aEntidad(EstudianteRequest solicitud) {
		return new Estudiante(
				solicitud.nombreCompleto(),
				solicitud.correo(),
				solicitud.nivelExperiencia(),
				solicitud.areaInteres());
	}

	public static EstudianteResponse aResponse(Estudiante estudiante) {
		return new EstudianteResponse(
				estudiante.getId(),
				estudiante.getNombreCompleto(),
				estudiante.getCorreo(),
				estudiante.getNivelExperiencia(),
				estudiante.getAreaInteres(),
				estudiante.getFechaRegistro());
	}

}
