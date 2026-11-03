package com.rutaia.aplicacion.servicio;

import com.rutaia.aplicacion.dto.calificacion.CalificacionRequest;
import com.rutaia.aplicacion.dto.calificacion.CalificacionResponse;

/** Calificación de las recomendaciones: una sola por recomendación (RF 17; E-13). */
public interface CalificacionServicio {

	/**
	 * @throws com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException si la recomendación no existe
	 * @throws com.rutaia.aplicacion.excepcion.ConflictoException si la recomendación ya fue calificada
	 */
	CalificacionResponse calificar(Long recomendacionId, CalificacionRequest solicitud);

}
