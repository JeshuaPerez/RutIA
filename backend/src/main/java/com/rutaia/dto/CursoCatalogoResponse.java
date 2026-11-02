package com.rutaia.dto;

import com.rutaia.entidad.Curso;
import com.rutaia.entidad.NivelCurso;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CursoCatalogo", description = "Curso activo del catálogo")
public record CursoCatalogoResponse(

		@Schema(example = "1")
		Long id,

		@Schema(example = "Fundamentos de HTML y CSS para páginas web")
		String nombre,

		@Schema(example = "Curso para quienes quieren aprender a crear páginas web desde cero...")
		String descripcion,

		@Schema(example = "Desarrollo web")
		String categoria,

		@Schema(example = "Básico", allowableValues = { "Básico", "Intermedio", "Avanzado" })
		NivelCurso nivel,

		@Schema(example = "40")
		Integer duracionHoras) {

	public static CursoCatalogoResponse desde(Curso curso) {
		return new CursoCatalogoResponse(
				curso.getId(),
				curso.getNombre(),
				curso.getDescripcion(),
				curso.getCategoria(),
				curso.getNivel(),
				curso.getDuracionHoras());
	}

}
