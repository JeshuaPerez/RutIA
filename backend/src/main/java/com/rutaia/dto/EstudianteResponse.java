package com.rutaia.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rutaia.entidad.Estudiante;
import com.rutaia.entidad.NivelExperiencia;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Estudiante", description = "Estudiante registrado")
public record EstudianteResponse(

		@Schema(example = "11")
		Long id,

		@Schema(example = "Ana María Torres")
		String nombreCompleto,

		@Schema(example = "ana.torres@rutaia.test")
		String correo,

		@Schema(example = "Principiante", allowableValues = { "Principiante", "Intermedio", "Avanzado" })
		NivelExperiencia nivelExperiencia,

		@Schema(example = "Desarrollo web")
		String areaInteres,

		@Schema(type = "string", example = "2026-11-02T17:05:12", description = "ISO-8601 sin zona, hora local")
		@JsonFormat(pattern = Formatos.FECHA_HORA)
		LocalDateTime fechaRegistro) {

	public static EstudianteResponse desde(Estudiante estudiante) {
		return new EstudianteResponse(
				estudiante.getId(),
				estudiante.getNombreCompleto(),
				estudiante.getCorreo(),
				estudiante.getNivelExperiencia(),
				estudiante.getAreaInteres(),
				estudiante.getFechaRegistro());
	}

}
