package com.rutaia.aplicacion.mapper;

import com.rutaia.aplicacion.dto.curso.CursoAdminResponse;
import com.rutaia.aplicacion.dto.curso.CursoCatalogoResponse;
import com.rutaia.aplicacion.dto.curso.CursoRequest;
import com.rutaia.dominio.Curso;

/** Conversión entre la entidad Curso y sus DTO. */
public final class CursoMapper {

	private CursoMapper() {
	}

	public static Curso aEntidad(CursoRequest solicitud) {
		return new Curso(
				solicitud.nombre(),
				solicitud.descripcion(),
				solicitud.categoria(),
				solicitud.nivel(),
				solicitud.duracionHoras());
	}

	/** Copia los datos de la solicitud sobre un curso existente; no cambia si está activo. */
	public static void actualizar(Curso curso, CursoRequest solicitud) {
		curso.actualizar(
				solicitud.nombre(),
				solicitud.descripcion(),
				solicitud.categoria(),
				solicitud.nivel(),
				solicitud.duracionHoras());
	}

	public static CursoCatalogoResponse aCatalogo(Curso curso) {
		return new CursoCatalogoResponse(
				curso.getId(),
				curso.getNombre(),
				curso.getDescripcion(),
				curso.getCategoria(),
				curso.getNivel(),
				curso.getDuracionHoras());
	}

	public static CursoAdminResponse aAdmin(Curso curso) {
		return new CursoAdminResponse(
				curso.getId(),
				curso.getNombre(),
				curso.getDescripcion(),
				curso.getCategoria(),
				curso.getNivel(),
				curso.getDuracionHoras(),
				curso.getActivo());
	}

}
