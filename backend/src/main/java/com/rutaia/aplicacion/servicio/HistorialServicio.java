package com.rutaia.aplicacion.servicio;

import java.util.List;

import com.rutaia.aplicacion.dto.consulta.ConsultaDetalleResponse;
import com.rutaia.aplicacion.dto.consulta.HistorialItemResponse;

/** Lectura de consultas ya registradas: historial (RF 16; E-04) y detalle (RF 02; E-12). */
public interface HistorialServicio {

	/**
	 * Consultas del estudiante por fecha descendente.
	 *
	 * @throws com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException si el estudiante no existe
	 */
	List<HistorialItemResponse> historial(Long estudianteId);

	/**
	 * Lo mismo que devolvió E-11 más la calificación de la recomendación.
	 *
	 * @throws com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException si la consulta no existe
	 */
	ConsultaDetalleResponse detalle(Long consultaId);

}
