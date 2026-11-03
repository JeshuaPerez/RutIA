package com.rutaia.aplicacion.dto.consulta;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rutaia.aplicacion.mapper.Formatos;
import com.rutaia.dominio.Consulta;
import com.rutaia.dominio.Curso;
import com.rutaia.dominio.EstadoConsulta;
import com.rutaia.dominio.Recomendacion;
import com.rutaia.dominio.RecomendacionFuente;

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

	static final int LARGO_RESUMEN = 160;
	static final String CORTE = "…";

	/**
	 * @param recomendacion la de la consulta, o null si no terminó Respondida
	 * @param fuentes las de esa recomendación, ya ordenadas por similitud descendente
	 */
	public static HistorialItemResponse desde(Consulta consulta, Recomendacion recomendacion, List<RecomendacionFuente> fuentes) {
		String resumen = switch (consulta.getEstado()) {
			case RESPONDIDA -> resumir(recomendacion.getRespuesta());
			case SIN_RESULTADOS -> ConsultaResultadoResponse.MSG_SIN_RESULTADOS;
			case ERROR -> ConsultaResultadoResponse.MSG_ERROR;
			case PENDIENTE -> ConsultaResultadoResponse.MSG_PENDIENTE;
		};
		return new HistorialItemResponse(
				consulta.getId(),
				consulta.getFecha(),
				consulta.getPregunta(),
				consulta.getEstado(),
				resumen,
				fuentes.stream().map(fuente -> CursoRecomendado.desde(fuente.getCurso())).toList());
	}

	/** Cuenta caracteres (code points), no unidades UTF-16, para no partir un emoji. */
	static String resumir(String respuesta) {
		if (respuesta.codePointCount(0, respuesta.length()) <= LARGO_RESUMEN) {
			return respuesta;
		}
		return respuesta.substring(0, respuesta.offsetByCodePoints(0, LARGO_RESUMEN)) + CORTE;
	}

	@Schema(name = "CursoRecomendado", description = "Curso usado como fuente de la recomendación")
	public record CursoRecomendado(

			@Schema(example = "1")
			Long cursoId,

			@Schema(example = "Fundamentos de HTML y CSS para páginas web")
			String nombre) {

		static CursoRecomendado desde(Curso curso) {
			return new CursoRecomendado(curso.getId(), curso.getNombre());
		}

	}

}
