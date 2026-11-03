package com.rutaia.aplicacion.servicio.impl;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.aplicacion.dto.calificacion.CalificacionRequest;
import com.rutaia.aplicacion.dto.calificacion.CalificacionResponse;
import com.rutaia.dominio.Calificacion;
import com.rutaia.dominio.Recomendacion;
import com.rutaia.aplicacion.excepcion.ConflictoException;
import com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException;
import com.rutaia.persistencia.CalificacionRepositorio;
import com.rutaia.persistencia.RecomendacionRepositorio;

/** E-13: una sola calificación por recomendación (RF 17). */
@Service
public class CalificacionServicioImpl {

	private static final String RESTRICCION_CALIFICACION_UNICA = "uq_calificacion_recomendacion";

	private final RecomendacionRepositorio recomendacionRepositorio;
	private final CalificacionRepositorio calificacionRepositorio;

	public CalificacionServicioImpl(RecomendacionRepositorio recomendacionRepositorio,
			CalificacionRepositorio calificacionRepositorio) {
		this.recomendacionRepositorio = recomendacionRepositorio;
		this.calificacionRepositorio = calificacionRepositorio;
	}

	@Transactional
	public CalificacionResponse calificar(Long recomendacionId, CalificacionRequest solicitud) {
		Recomendacion recomendacion = recomendacionRepositorio.findById(recomendacionId)
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"No existe una recomendación con id " + recomendacionId));
		if (calificacionRepositorio.existsByRecomendacionId(recomendacionId)) {
			throw yaCalificada(recomendacionId);
		}
		Calificacion calificacion = new Calificacion(recomendacion, solicitud.puntuacion(), solicitud.comentario());
		try {
			// saveAndFlush para que una calificación concurrente de la misma recomendación falle aquí y no al confirmar.
			return CalificacionResponse.desde(calificacionRepositorio.saveAndFlush(calificacion));
		}
		catch (DataIntegrityViolationException ex) {
			if (esViolacionDeCalificacionUnica(ex)) {
				throw yaCalificada(recomendacionId);
			}
			throw ex;
		}
	}

	private static ConflictoException yaCalificada(Long recomendacionId) {
		return new ConflictoException("La recomendación " + recomendacionId + " ya fue calificada", null);
	}

	private static boolean esViolacionDeCalificacionUnica(DataIntegrityViolationException ex) {
		for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
			if (causa instanceof ConstraintViolationException violacion) {
				return RESTRICCION_CALIFICACION_UNICA.equalsIgnoreCase(violacion.getConstraintName());
			}
		}
		return false;
	}

}
