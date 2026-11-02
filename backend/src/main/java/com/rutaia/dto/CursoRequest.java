package com.rutaia.dto;

import com.rutaia.entidad.NivelCurso;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar o actualizar un curso")
public record CursoRequest(

		@Schema(example = "Introducción a TypeScript", maxLength = 150)
		@NotBlank(message = "es obligatorio")
		@Size(max = 150, message = "debe tener como máximo 150 caracteres")
		String nombre,

		@Schema(example = "Curso práctico sobre tipos, interfaces y genéricos en TypeScript para crear aplicaciones web mantenibles.")
		@NotBlank(message = "es obligatorio")
		String descripcion,

		@Schema(example = "Desarrollo web", maxLength = 80)
		@NotBlank(message = "es obligatorio")
		@Size(max = 80, message = "debe tener como máximo 80 caracteres")
		String categoria,

		@Schema(example = "Intermedio", allowableValues = { "Básico", "Intermedio", "Avanzado" })
		@NotNull(message = "es obligatorio")
		NivelCurso nivel,

		@Schema(example = "24", minimum = "1")
		@NotNull(message = "es obligatorio")
		@Positive(message = "debe ser mayor que cero")
		Integer duracionHoras) {
}
