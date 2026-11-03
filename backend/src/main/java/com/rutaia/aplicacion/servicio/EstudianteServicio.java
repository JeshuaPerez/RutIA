package com.rutaia.aplicacion.servicio;

import java.util.List;

import com.rutaia.aplicacion.dto.estudiante.EstudianteRequest;
import com.rutaia.aplicacion.dto.estudiante.EstudianteResponse;

/** Registro y consulta de estudiantes (RF 01 y RF 02; E-01 a E-03). */
public interface EstudianteServicio {

	/** @throws com.rutaia.aplicacion.excepcion.ConflictoException si el correo ya está registrado */
	EstudianteResponse registrar(EstudianteRequest solicitud);

	/** Todos los estudiantes por id ascendente. */
	List<EstudianteResponse> listar();

	/** @throws com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException si no existe */
	EstudianteResponse consultar(Long id);

}
