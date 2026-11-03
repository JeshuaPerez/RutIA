package com.rutaia.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rutaia.dominio.Consulta;
import com.rutaia.dominio.EstadoConsulta;

public interface ConsultaRepositorio extends JpaRepository<Consulta, Long> {

	/** Historial (E-04): usa idx_consulta_estudiante_fecha; el id desempata las consultas del mismo segundo. */
	@Query("""
			select c from Consulta c
			where c.estudiante.id = :estudianteId
			order by c.fecha desc, c.id desc
			""")
	List<Consulta> buscarHistorial(@Param("estudianteId") Long estudianteId);

	/** Estadísticas (E-14): los tres conteos en una sola lectura de la tabla. */
	@Query("""
			select count(c) as total,
			       count(case when c.estado = :respondida then 1 end) as respondidas,
			       count(case when c.estado = :sinResultados then 1 end) as sinResultados
			from Consulta c
			""")
	ConteoConsultas contarPorEstado(@Param("respondida") EstadoConsulta respondida,
			@Param("sinResultados") EstadoConsulta sinResultados);

	interface ConteoConsultas {

		Long getTotal();

		Long getRespondidas();

		Long getSinResultados();

	}

}
