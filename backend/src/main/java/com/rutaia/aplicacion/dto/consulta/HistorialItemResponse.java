package com.rutaia.aplicacion.dto.consulta;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rutaia.aplicacion.mapper.Formatos;
import com.rutaia.dominio.EstadoConsulta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "HistorialItem", description = "Registro del historial de un estudiante (RF 16)")
public record HistorialItemResponse(

		@Schema(example = "2")
		Long consultaId,

		@Schema(type = "string", example = "2026-11-02T22:34:20", description = "ISO-8601 sin zona, hora local")
		@JsonFormat(pattern = Formatos.FECHA_HORA)
		LocalDateTime fecha,

		@Schema(example = "Quiero aprender a crear páginas web.")
		String pregunta,

		@Schema(example = "Respondida", allowableValues = { "Pendiente", "Respondida", "Sin resultados", "Error" })
		EstadoConsulta estado,

		@Schema(description = "Respondida: los primeros 160 caracteres de la respuesta, más «…» si se cortó. "
				+ "Sin resultados, Error y Pendiente: el mensaje fijo de C6")
		String resumen,

		@Schema(description = "Fuentes de la recomendación por similitud descendente; [] si no hay")
		List<CursoRecomendado> cursosRecomendados) {

	@Schema(name = "CursoRecomendado", description = "Curso usado como fuente de la recomendación")
	public record CursoRecomendado(

			@Schema(example = "1")
			Long cursoId,

			@Schema(example = "Fundamentos de HTML y CSS para páginas web")
			String nombre) {
	}

}
