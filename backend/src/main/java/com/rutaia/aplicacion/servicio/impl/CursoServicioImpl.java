package com.rutaia.aplicacion.servicio.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.aplicacion.dto.curso.CursoAdminResponse;
import com.rutaia.aplicacion.dto.curso.CursoCatalogoResponse;
import com.rutaia.aplicacion.dto.curso.CursoRequest;
import com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException;
import com.rutaia.aplicacion.mapper.CursoMapper;
import com.rutaia.aplicacion.servicio.AdministracionCursoServicio;
import com.rutaia.aplicacion.servicio.CatalogoCursoServicio;
import com.rutaia.dominio.Curso;
import com.rutaia.dominio.NivelCurso;
import com.rutaia.persistencia.CursoRepositorio;

/** Una sola clase para los cursos; cada controlador la ve solo por la interfaz que usa (catálogo o administración). */
@Service
public class CursoServicioImpl implements CatalogoCursoServicio, AdministracionCursoServicio {

	private final CursoRepositorio cursoRepositorio;

	public CursoServicioImpl(CursoRepositorio cursoRepositorio) {
		this.cursoRepositorio = cursoRepositorio;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CursoCatalogoResponse> listarCatalogo(String categoria, NivelCurso nivel) {
		String filtroCategoria = categoria == null || categoria.isBlank() ? null : categoria;
		return cursoRepositorio.buscarCatalogo(filtroCategoria, nivel).stream()
				.map(CursoMapper::aCatalogo)
				.toList();
	}

	@Override
	@Transactional
	public CursoAdminResponse registrar(CursoRequest solicitud) {
		return CursoMapper.aAdmin(cursoRepositorio.save(CursoMapper.aEntidad(solicitud)));
	}

	@Override
	@Transactional(readOnly = true)
	public List<CursoAdminResponse> listarTodos() {
		return cursoRepositorio.findAllByOrderByIdAsc().stream()
				.map(CursoMapper::aAdmin)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CursoAdminResponse consultar(Long id) {
		return CursoMapper.aAdmin(buscar(id));
	}

	@Override
	@Transactional
	public CursoAdminResponse actualizar(Long id, CursoRequest solicitud) {
		Curso curso = buscar(id);
		CursoMapper.actualizar(curso, solicitud);
		return CursoMapper.aAdmin(cursoRepositorio.saveAndFlush(curso));
	}

	@Override
	@Transactional
	public CursoAdminResponse desactivar(Long id) {
		Curso curso = buscar(id);
		curso.desactivar();
		return CursoMapper.aAdmin(cursoRepositorio.saveAndFlush(curso));
	}

	private Curso buscar(Long id) {
		return cursoRepositorio.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe un curso con id " + id));
	}

}
