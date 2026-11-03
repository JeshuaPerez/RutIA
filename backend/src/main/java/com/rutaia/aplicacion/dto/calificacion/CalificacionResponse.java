package com.rutaia.aplicacion.dto.calificacion;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rutaia.aplicacion.mapper.Formatos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Calificacion", description = "Calificación registrada de una recomendación")
public record CalificacionResponse(

		@Schema(example = "1")
		Long recomendacionId,

		@Schema(example = "5", minimum = "1", maximum = "5")
		Integer puntuacion,

		@Schema(example = "La ruta me sirvió para saber por dónde empezar.", nullable = true)
		String comentario,

		@Schema(type = "string", example = "2026-11-02T23:10:05", description = "ISO-8601 sin zona, hora local")
		@JsonFormat(pattern = Formatos.FECHA_HORA)
		LocalDateTime fecha) {
}
