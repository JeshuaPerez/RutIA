package com.rutaia.aplicacion.mapper;

import java.util.List;

import com.rutaia.aplicacion.dto.consulta.ConsultaDetalleResponse;
import com.rutaia.aplicacion.dto.consulta.ConsultaResultadoResponse;
import com.rutaia.aplicacion.dto.consulta.FuenteResponse;
import com.rutaia.aplicacion.dto.consulta.HistorialItemResponse;
import com.rutaia.dominio.Calificacion;
import com.rutaia.dominio.Consulta;
import com.rutaia.dominio.Curso;
import com.rutaia.dominio.Recomendacion;
import com.rutaia.dominio.RecomendacionFuente;

/**
 * Conversión de una consulta, su recomendación y sus fuentes a los DTO de E-11 (resultado), E-12 (detalle) y E-04
 * (historial). E-11 y E-12 usan las mismas fábricas, así que devuelven exactamente lo mismo.
 */
public final class ConsultaMapper {

	public static final String MSG_SIN_RESULTADOS =
			"No encontramos cursos del catálogo relacionados con tu necesidad. Intenta describirla con otras palabras.";
	public static final String MSG_ERROR =
			"No fue posible generar la recomendación en este momento. Intenta de nuevo más tarde.";
	public static final String MSG_PENDIENTE = "Consulta en proceso.";

	static final int LARGO_RESUMEN = 160;
	static final String CORTE = "…";

	private ConsultaMapper() {
	}

	/** @param fuentes las de la recomendación, ya ordenadas por similitud descendente y con su curso cargado */
	public static ConsultaResultadoResponse respondida(Consulta consulta, Recomendacion recomendacion,
			List<RecomendacionFuente> fuentes) {
		return new ConsultaResultadoResponse(
				consulta.getId(),
				consulta.getEstudiante().getId(),
				consulta.getPregunta(),
				consulta.getFecha(),
				consulta.getEstado(),
				recomendacion.getRespuesta(),
				null,
				recomendacion.getId(),
				fuentes.stream().map(ConsultaMapper::aFuente).toList());
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

	/** ConsultaResultadoResponse (C7) más la calificación de la recomendación, o null si no tiene. */
	public static ConsultaDetalleResponse aDetalle(ConsultaResultadoResponse resultado, Calificacion calificacion) {
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
				calificacion == null ? null : CalificacionMapper.aDetalle(calificacion));
	}

	/**
	 * @param recomendacion la de la consulta, o null si no terminó Respondida
	 * @param fuentes las de esa recomendación, ya ordenadas por similitud descendente
	 */
	public static HistorialItemResponse aHistorialItem(Consulta consulta, Recomendacion recomendacion,
			List<RecomendacionFuente> fuentes) {
		String resumen = switch (consulta.getEstado()) {
			case RESPONDIDA -> resumir(recomendacion.getRespuesta());
			case SIN_RESULTADOS -> MSG_SIN_RESULTADOS;
			case ERROR -> MSG_ERROR;
			case PENDIENTE -> MSG_PENDIENTE;
		};
		return new HistorialItemResponse(
				consulta.getId(),
				consulta.getFecha(),
				consulta.getPregunta(),
				consulta.getEstado(),
				resumen,
				fuentes.stream().map(fuente -> aCursoRecomendado(fuente.getCurso())).toList());
	}

	public static FuenteResponse aFuente(RecomendacionFuente fuente) {
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

	/** Cuenta caracteres (code points), no unidades UTF-16, para no partir un emoji. */
	static String resumir(String respuesta) {
		if (respuesta.codePointCount(0, respuesta.length()) <= LARGO_RESUMEN) {
			return respuesta;
		}
		return respuesta.substring(0, respuesta.offsetByCodePoints(0, LARGO_RESUMEN)) + CORTE;
	}

	private static HistorialItemResponse.CursoRecomendado aCursoRecomendado(Curso curso) {
		return new HistorialItemResponse.CursoRecomendado(curso.getId(), curso.getNombre());
	}

}
