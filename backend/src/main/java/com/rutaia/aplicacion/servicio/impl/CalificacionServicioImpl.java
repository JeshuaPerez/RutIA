package com.rutaia.aplicacion.servicio.impl;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.aplicacion.dto.calificacion.CalificacionRequest;
import com.rutaia.aplicacion.dto.calificacion.CalificacionResponse;
import com.rutaia.aplicacion.excepcion.ConflictoException;
import com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException;
import com.rutaia.aplicacion.mapper.CalificacionMapper;
import com.rutaia.aplicacion.servicio.CalificacionServicio;
import com.rutaia.aplicacion.servicio.RecomendacionServicio;
import com.rutaia.dominio.Recomendacion;
import com.rutaia.persistencia.CalificacionRepositorio;

/** E-13: una sola calificación por recomendación (RF 17). */
@Service
public class CalificacionServicioImpl implements CalificacionServicio {

	private static final String RESTRICCION_CALIFICACION_UNICA = "uq_calificacion_recomendacion";

	private final RecomendacionServicio recomendacionServicio;
	private final CalificacionRepositorio calificacionRepositorio;

	public CalificacionServicioImpl(RecomendacionServicio recomendacionServicio,
			CalificacionRepositorio calificacionRepositorio) {
		this.recomendacionServicio = recomendacionServicio;
		this.calificacionRepositorio = calificacionRepositorio;
	}

	@Override
	@Transactional
	public CalificacionResponse calificar(Long recomendacionId, CalificacionRequest solicitud) {
		Recomendacion recomendacion = recomendacionServicio.buscar(recomendacionId)
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"No existe una recomendación con id " + recomendacionId));
		if (calificacionRepositorio.existsByRecomendacionId(recomendacionId)) {
			throw yaCalificada(recomendacionId);
		}
		try {
			// saveAndFlush para que una calificación concurrente de la misma recomendación falle aquí y no al confirmar.
			return CalificacionMapper.aResponse(
					calificacionRepositorio.saveAndFlush(CalificacionMapper.aEntidad(recomendacion, solicitud)));
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
