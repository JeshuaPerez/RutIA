package com.rutaia.servicio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.dto.CursoAdminResponse;
import com.rutaia.dto.CursoCatalogoResponse;
import com.rutaia.dto.CursoRequest;
import com.rutaia.entidad.Curso;
import com.rutaia.entidad.NivelCurso;
import com.rutaia.excepcion.RecursoNoEncontradoException;
import com.rutaia.repositorio.CursoRepositorio;

@Service
public class CursoServicio {

	private final CursoRepositorio cursoRepositorio;

	public CursoServicio(CursoRepositorio cursoRepositorio) {
		this.cursoRepositorio = cursoRepositorio;
	}

	@Transactional(readOnly = true)
	public List<CursoCatalogoResponse> listarCatalogo(String categoria, NivelCurso nivel) {
		String filtroCategoria = categoria == null || categoria.isBlank() ? null : categoria;
		return cursoRepositorio.buscarCatalogo(filtroCategoria, nivel).stream()
				.map(CursoCatalogoResponse::desde)
				.toList();
	}

	@Transactional
	public CursoAdminResponse registrar(CursoRequest solicitud) {
		Curso curso = new Curso(
				solicitud.nombre(),
				solicitud.descripcion(),
				solicitud.categoria(),
				solicitud.nivel(),
				solicitud.duracionHoras());
		return CursoAdminResponse.desde(cursoRepositorio.save(curso));
	}

	@Transactional(readOnly = true)
	public List<CursoAdminResponse> listarTodos() {
		return cursoRepositorio.findAllByOrderByIdAsc().stream()
				.map(CursoAdminResponse::desde)
				.toList();
	}

	@Transactional(readOnly = true)
	public CursoAdminResponse consultar(Long id) {
		return CursoAdminResponse.desde(buscar(id));
	}

	@Transactional
	public CursoAdminResponse actualizar(Long id, CursoRequest solicitud) {
		Curso curso = buscar(id);
		curso.actualizar(
				solicitud.nombre(),
				solicitud.descripcion(),
				solicitud.categoria(),
				solicitud.nivel(),
				solicitud.duracionHoras());
		return CursoAdminResponse.desde(cursoRepositorio.saveAndFlush(curso));
	}

	/** Idempotente: desactivar un curso inactivo lo deja igual. */
	@Transactional
	public CursoAdminResponse desactivar(Long id) {
		Curso curso = buscar(id);
		curso.desactivar();
		return CursoAdminResponse.desde(cursoRepositorio.saveAndFlush(curso));
	}

	private Curso buscar(Long id) {
		return cursoRepositorio.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un curso con id " + id));
	}

}
