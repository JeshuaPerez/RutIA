package com.rutaia.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rutaia.entidad.Curso;
import com.rutaia.entidad.NivelCurso;

public interface CursoRepositorio extends JpaRepository<Curso, Long> {

	List<Curso> findAllByOrderByIdAsc();

	/** Catálogo (RF 04): solo activos; cada filtro se ignora si llega nulo. */
	@Query("""
			select c from Curso c
			where c.activo = true
			  and (:categoria is null or c.categoria = :categoria)
			  and (:nivel is null or c.nivel = :nivel)
			order by c.id asc
			""")
	List<Curso> buscarCatalogo(@Param("categoria") String categoria, @Param("nivel") NivelCurso nivel);

}
