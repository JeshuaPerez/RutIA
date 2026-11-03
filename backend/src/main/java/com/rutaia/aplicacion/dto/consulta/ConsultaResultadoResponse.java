package com.rutaia.aplicacion.dto.consulta;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rutaia.aplicacion.mapper.Formatos;
import com.rutaia.dominio.EstadoConsulta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ConsultaResultado", description = "Consulta con su estado final y, si fue Respondida, la recomendación")
public record ConsultaResultadoResponse(

		@Schema(example = "12")
		Long consultaId,

		@Schema(example = "1")
		Long estudianteId,

		@Schema(example = "Quiero aprender a crear páginas web.")
		String pregunta,

		@Schema(type = "string", example = "2026-11-01T10:32:00", description = "ISO-8601 sin zona, hora local")
		@JsonFormat(pattern = Formatos.FECHA_HORA)
		LocalDateTime fecha,

		@Schema(example = "Respondida", allowableValues = { "Pendiente", "Respondida", "Sin resultados", "Error" })
		EstadoConsulta estado,

		@Schema(example = "Te sugiero empezar por…", nullable = true,
				description = "Texto plano del modelo; null si el estado no es Respondida")
		String respuesta,

		@Schema(nullable = true,
				description = "Mensaje para el estudiante cuando no hay respuesta; null si el estado es Respondida")
		String mensaje,

		@Schema(example = "7", nullable = true, description = "null si el estado no es Respondida")
		Long recomendacionId,

		@Schema(description = "De 1 a 5 fuentes por similitud descendente si es Respondida; [] en otro caso")
		List<FuenteResponse> fuentes) {
}
