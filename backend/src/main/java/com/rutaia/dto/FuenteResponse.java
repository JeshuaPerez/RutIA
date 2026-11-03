package com.rutaia.dto;

import com.rutaia.entidad.Curso;
import com.rutaia.entidad.NivelCurso;
import com.rutaia.entidad.RecomendacionFuente;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Fuente", description = "Curso del catálogo usado como fuente de la recomendación (datos de PostgreSQL)")
public record FuenteResponse(

		@Schema(example = "1")
		Long cursoId,

		@Schema(example = "Fundamentos de HTML y CSS para páginas web")
		String nombre,

		@Schema(example = "Curso para quienes quieren aprender a crear páginas web desde cero...")
		String descripcion,

		@Schema(example = "Desarrollo web")
		String categoria,

		@Schema(example = "Básico", allowableValues = { "Básico", "Intermedio", "Avanzado" })
		NivelCurso nivel,

		@Schema(example = "40")
		Integer duracionHoras,

		@Schema(example = "0.6421", minimum = "0", maximum = "1",
				description = "Similitud de Qdrant entre la pregunta y el curso, con 4 decimales")
		Double similitud) {

	public static FuenteResponse desde(RecomendacionFuente fuente) {
		Curso curso = fuente.getCurso();
		return new FuenteResponse(
				curso.getId(),
				curso.getNombre(),
				curso.getDescripcion(),
				curso.getCategoria(),
				curso.getNivel(),
				curso.getDuracionHoras(),
				fuente.getSimilitud());
	}

}
