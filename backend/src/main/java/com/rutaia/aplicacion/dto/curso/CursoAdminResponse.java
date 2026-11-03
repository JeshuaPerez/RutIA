package com.rutaia.aplicacion.dto.curso;

import com.rutaia.dominio.NivelCurso;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CursoAdmin", description = "Curso con su estado, para administración")
public record CursoAdminResponse(

		@Schema(example = "31")
		Long id,

		@Schema(example = "Introducción a TypeScript")
		String nombre,

		@Schema(example = "Curso práctico sobre tipos, interfaces y genéricos en TypeScript para crear aplicaciones web mantenibles.")
		String descripcion,

		@Schema(example = "Desarrollo web")
		String categoria,

		@Schema(example = "Intermedio", allowableValues = { "Básico", "Intermedio", "Avanzado" })
		NivelCurso nivel,

		@Schema(example = "24")
		Integer duracionHoras,

		@Schema(example = "true")
		Boolean activo) {
}
