package com.rutaia.aplicacion.servicio;

import java.util.List;

import com.rutaia.aplicacion.dto.curso.CursoCatalogoResponse;
import com.rutaia.dominio.NivelCurso;

/** Catálogo público: solo lectura de los cursos activos (RF 04; E-05). */
public interface CatalogoCursoServicio {

	/**
	 * Cursos activos por id ascendente. Cada filtro es opcional y se ignora si llega nulo; la categoría también si
	 * llega vacía.
	 */
	List<CursoCatalogoResponse> listarCatalogo(String categoria, NivelCurso nivel);

}
