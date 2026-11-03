package com.rutaia.repositorio;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rutaia.entidad.RecomendacionFuente;

public interface RecomendacionFuenteRepositorio extends JpaRepository<RecomendacionFuente, Long> {

	/**
	 * Fuentes de una recomendación con su curso (E-12), por similitud descendente. El id desempata igual que E-11,
	 * que las guardó en ese orden.
	 */
	@Query("""
			select f from RecomendacionFuente f
			join fetch f.curso
			where f.recomendacion.id = :recomendacionId
			order by f.similitud desc, f.id asc
			""")
	List<RecomendacionFuente> buscarPorRecomendacion(@Param("recomendacionId") Long recomendacionId);

	/** Fuentes de todas las recomendaciones del estudiante con su curso, en una sola consulta (E-04). */
	@Query("""
			select f from RecomendacionFuente f
			join fetch f.curso
			where f.recomendacion.consulta.estudiante.id = :estudianteId
			order by f.similitud desc, f.id asc
			""")
	List<RecomendacionFuente> buscarPorEstudiante(@Param("estudianteId") Long estudianteId);

	/** Cursos por cantidad de filas en recomendacion_fuente (E-14); en empate, el menor id. Usa idx_fuente_curso. */
	@Query("""
			select cu.id as cursoId, cu.nombre as nombre, count(f) as vecesRecomendado
			from RecomendacionFuente f
			join f.curso cu
			group by cu.id, cu.nombre
			order by count(f) desc, cu.id asc
			""")
	List<CursoConteo> buscarMasRecomendados(Limit limite);

	interface CursoConteo {

		Long getCursoId();

		String getNombre();

		Long getVecesRecomendado();

	}

}
