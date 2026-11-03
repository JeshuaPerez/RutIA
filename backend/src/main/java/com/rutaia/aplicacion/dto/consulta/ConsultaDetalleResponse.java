package com.rutaia.aplicacion.dto.consulta;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rutaia.aplicacion.mapper.Formatos;
import com.rutaia.dominio.Calificacion;
import com.rutaia.dominio.EstadoConsulta;

import io.swagger.v3.oas.annotations.media.Schema;

/** ConsultaResultadoResponse (C7) más la calificación de la recomendación. */
@Schema(name = "ConsultaDetalle", description = "Detalle completo de una consulta: lo mismo que devolvió E-11 más la "
		+ "calificación de la recomendación")
public record ConsultaDetalleResponse(

		@Schema(example = "2")
		Long consultaId,

		@Schema(example = "1")
		Long estudianteId,

		@Schema(example = "Quiero aprender a crear páginas web.")
		String pregunta,

		@Schema(type = "string", example = "2026-11-02T22:34:20", description = "ISO-8601 sin zona, hora local")
		@JsonFormat(pattern = Formatos.FECHA_HORA)
		LocalDateTime fecha,

		@Schema(example = "Respondida", allowableValues = { "Pendiente", "Respondida", "Sin resultados", "Error" })
		EstadoConsulta estado,

		@Schema(example = "Hola, soy tu orientador de RutaIA…", nullable = true,
				description = "Texto plano del modelo; null si el estado no es Respondida")
		String respuesta,

		@Schema(nullable = true,
				description = "Mensaje para el estudiante cuando no hay respuesta; null si el estado es Respondida")
		String mensaje,

		@Schema(example = "1", nullable = true, description = "null si el estado no es Respondida")
		Long recomendacionId,

		@Schema(description = "De 1 a 5 fuentes por similitud descendente si es Respondida; [] en otro caso")
		List<FuenteResponse> fuentes,

		@Schema(nullable = true, description = "null si la recomendación no tiene calificación o no hay recomendación")
		CalificacionDetalle calificacion) {

	public static ConsultaDetalleResponse desde(ConsultaResultadoResponse resultado, Calificacion calificacion) {
		return new ConsultaDetalleResponse(
				resultado.consultaId(),
				resultado.estudianteId(),
				resultado.pregunta(),
				resultado.fecha(),
				resultado.estado(),
				resultado.respuesta(),
				resultado.mensaje(),
				resultado.recomendacionId(),
				resultado.fuentes(),
				calificacion == null ? null : CalificacionDetalle.desde(calificacion));
	}

	@Schema(name = "CalificacionDetalle", description = "Calificación de la recomendación de la consulta")
	public record CalificacionDetalle(

			@Schema(example = "5", minimum = "1", maximum = "5")
			Integer puntuacion,

			@Schema(example = "La ruta me sirvió para saber por dónde empezar.", nullable = true)
			String comentario,

			@Schema(type = "string", example = "2026-11-02T23:10:05", description = "ISO-8601 sin zona, hora local")
			@JsonFormat(pattern = Formatos.FECHA_HORA)
			LocalDateTime fecha) {

		static CalificacionDetalle desde(Calificacion calificacion) {
			return new CalificacionDetalle(calificacion.getPuntuacion(), calificacion.getComentario(),
					calificacion.getFecha());
		}

	}

}
