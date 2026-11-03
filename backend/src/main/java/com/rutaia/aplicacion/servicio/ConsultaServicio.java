package com.rutaia.aplicacion.servicio;

import com.rutaia.aplicacion.dto.consulta.ConsultaRequest;
import com.rutaia.aplicacion.dto.consulta.ConsultaResultadoResponse;

/** Registro de consultas y su recomendación (RF 06 a RF 08, RF 13 y RF 14; E-11). */
public interface ConsultaServicio {

	/**
	 * Guarda la consulta, pide la recomendación al motor y devuelve el estado final: Respondida, Sin resultados o
	 * Error. Ninguna consulta queda en Pendiente.
	 *
	 * @throws com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException si el estudiante no existe; no se crea nada
	 */
	ConsultaResultadoResponse registrar(ConsultaRequest solicitud);

}
