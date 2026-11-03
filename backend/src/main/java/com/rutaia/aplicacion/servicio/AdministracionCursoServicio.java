package com.rutaia.aplicacion.servicio;

import java.util.List;

import com.rutaia.aplicacion.dto.curso.CursoAdminResponse;
import com.rutaia.aplicacion.dto.curso.CursoRequest;

/**
 * Administración de cursos, activos e inactivos (RF 03; E-06 a E-10). Los métodos que reciben un id lanzan
 * {@link com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException} si el curso no existe.
 */
public interface AdministracionCursoServicio {

	/** Registra un curso activo. */
	CursoAdminResponse registrar(CursoRequest solicitud);

	/** Todos los cursos por id ascendente. */
	List<CursoAdminResponse> listarTodos();

	CursoAdminResponse consultar(Long id);

	/** Reemplaza los datos del curso; no cambia si está activo. */
	CursoAdminResponse actualizar(Long id, CursoRequest solicitud);

	/** Idempotente: desactivar un curso inactivo lo deja igual. */
	CursoAdminResponse desactivar(Long id);

}
