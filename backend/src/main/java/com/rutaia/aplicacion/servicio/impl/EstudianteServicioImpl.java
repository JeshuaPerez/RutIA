package com.rutaia.aplicacion.servicio.impl;

import java.util.List;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.aplicacion.dto.estudiante.EstudianteRequest;
import com.rutaia.aplicacion.dto.estudiante.EstudianteResponse;
import com.rutaia.dominio.Estudiante;
import com.rutaia.aplicacion.excepcion.ConflictoException;
import com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException;
import com.rutaia.persistencia.EstudianteRepositorio;

@Service
public class EstudianteServicioImpl {

	private static final String RESTRICCION_CORREO_UNICO = "uq_estudiante_correo";

	private final EstudianteRepositorio estudianteRepositorio;

	public EstudianteServicioImpl(EstudianteRepositorio estudianteRepositorio) {
		this.estudianteRepositorio = estudianteRepositorio;
	}

	@Transactional
	public EstudianteResponse registrar(EstudianteRequest solicitud) {
		// El DTO ya entrega el correo con trim y en minúsculas.
		if (estudianteRepositorio.existsByCorreo(solicitud.correo())) {
			throw correoDuplicado(solicitud.correo());
		}
		Estudiante estudiante = new Estudiante(
				solicitud.nombreCompleto(),
				solicitud.correo(),
				solicitud.nivelExperiencia(),
				solicitud.areaInteres());
		try {
			// saveAndFlush para que una alta concurrente con el mismo correo falle aquí y no al confirmar.
			return EstudianteResponse.desde(estudianteRepositorio.saveAndFlush(estudiante));
		}
		catch (DataIntegrityViolationException ex) {
			if (esViolacionDeCorreoUnico(ex)) {
				throw correoDuplicado(solicitud.correo());
			}
			throw ex;
		}
	}

	@Transactional(readOnly = true)
	public List<EstudianteResponse> listar() {
		return estudianteRepositorio.findAllByOrderByIdAsc().stream()
				.map(EstudianteResponse::desde)
				.toList();
	}

	@Transactional(readOnly = true)
	public EstudianteResponse consultar(Long id) {
		return estudianteRepositorio.findById(id)
				.map(EstudianteResponse::desde)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un estudiante con id " + id));
	}

	private static ConflictoException correoDuplicado(String correo) {
		return new ConflictoException("El correo " + correo + " ya está registrado", "correo");
	}

	private static boolean esViolacionDeCorreoUnico(DataIntegrityViolationException ex) {
		for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
			if (causa instanceof ConstraintViolationException violacion) {
				return RESTRICCION_CORREO_UNICO.equalsIgnoreCase(violacion.getConstraintName());
			}
		}
		return false;
	}

}
