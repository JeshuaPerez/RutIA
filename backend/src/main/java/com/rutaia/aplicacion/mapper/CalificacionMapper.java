package com.rutaia.aplicacion.mapper;

import com.rutaia.aplicacion.dto.calificacion.CalificacionRequest;
import com.rutaia.aplicacion.dto.calificacion.CalificacionResponse;
import com.rutaia.aplicacion.dto.consulta.ConsultaDetalleResponse;
import com.rutaia.dominio.Calificacion;
import com.rutaia.dominio.Recomendacion;

/** Conversión entre la entidad Calificacion y sus DTO. */
public final class CalificacionMapper {

	private CalificacionMapper() {
	}

	/** El DTO ya entrega el comentario con trim, o null si venía vacío. */
	public static Calificacion aEntidad(Recomendacion recomendacion, CalificacionRequest solicitud) {
		return new Calificacion(recomendacion, solicitud.puntuacion(), solicitud.comentario());
	}

	public static CalificacionResponse aResponse(Calificacion calificacion) {
		return new CalificacionResponse(
				calificacion.getRecomendacion().getId(),
				calificacion.getPuntuacion(),
				calificacion.getComentario(),
				calificacion.getFecha());
	}

	/** La calificación tal como aparece en el detalle de una consulta (E-12). */
	public static ConsultaDetalleResponse.CalificacionDetalle aDetalle(Calificacion calificacion) {
		return new ConsultaDetalleResponse.CalificacionDetalle(calificacion.getPuntuacion(), calificacion.getComentario(),
				calificacion.getFecha());
	}

}
