package com.rutaia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Estadisticas", description = "Estadísticas básicas de uso (RF 18)")
public record EstadisticasResponse(

		@Schema(example = "5", description = "Todas las consultas, en cualquier estado")
		Long totalConsultas,

		@Schema(example = "3")
		Long consultasRespondidas,

		@Schema(example = "1")
		Long consultasSinResultados,

		@Schema(example = "4.33", nullable = true, description = "Con 2 decimales; null si no hay calificaciones")
		Double promedioCalificaciones,

		@Schema(nullable = true, description = "Curso con más filas en recomendacion_fuente; en empate gana el menor "
				+ "cursoId; null si no hay recomendaciones")
		CursoMasRecomendado cursoMasRecomendado) {

	@Schema(name = "CursoMasRecomendado", description = "Curso que más veces fue fuente de una recomendación")
	public record CursoMasRecomendado(

			@Schema(example = "1")
			Long cursoId,

			@Schema(example = "Fundamentos de HTML y CSS para páginas web")
			String nombre,

			@Schema(example = "1")
			Long vecesRecomendado) {
	}

}
