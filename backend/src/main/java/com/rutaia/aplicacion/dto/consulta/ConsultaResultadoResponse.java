package com.rutaia.aplicacion.dto.consulta;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rutaia.aplicacion.mapper.Formatos;
import com.rutaia.dominio.Consulta;
import com.rutaia.dominio.EstadoConsulta;
import com.rutaia.dominio.Recomendacion;

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

	public static final String MSG_SIN_RESULTADOS =
			"No encontramos cursos del catálogo relacionados con tu necesidad. Intenta describirla con otras palabras.";
	public static final String MSG_ERROR =
			"No fue posible generar la recomendación en este momento. Intenta de nuevo más tarde.";
	public static final String MSG_PENDIENTE = "Consulta en proceso.";

	public static ConsultaResultadoResponse respondida(Consulta consulta, Recomendacion recomendacion,
			List<FuenteResponse> fuentes) {
		return new ConsultaResultadoResponse(
				consulta.getId(),
				consulta.getEstudiante().getId(),
				consulta.getPregunta(),
				consulta.getFecha(),
				consulta.getEstado(),
				recomendacion.getRespuesta(),
				null,
				recomendacion.getId(),
				fuentes);
	}

	/** Pendiente, Sin resultados o Error: sin respuesta ni fuentes, con el mensaje fijo de C6. */
	public static ConsultaResultadoResponse sinRecomendacion(Consulta consulta) {
		String mensaje = switch (consulta.getEstado()) {
			case PENDIENTE -> MSG_PENDIENTE;
			case SIN_RESULTADOS -> MSG_SIN_RESULTADOS;
			case ERROR -> MSG_ERROR;
			case RESPONDIDA -> throw new IllegalArgumentException("Una consulta Respondida tiene recomendación");
		};
		return new ConsultaResultadoResponse(
				consulta.getId(),
				consulta.getEstudiante().getId(),
				consulta.getPregunta(),
				consulta.getFecha(),
				consulta.getEstado(),
				null,
				mensaje,
				null,
				List.of());
	}

}
