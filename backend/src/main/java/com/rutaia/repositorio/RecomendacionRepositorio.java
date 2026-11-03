package com.rutaia.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rutaia.entidad.Recomendacion;

public interface RecomendacionRepositorio extends JpaRepository<Recomendacion, Long> {

	Optional<Recomendacion> findByConsultaId(Long consultaId);

	/** Recomendaciones de todas las consultas del estudiante, en una sola consulta (E-04). */
	@Query("select r from Recomendacion r where r.consulta.estudiante.id = :estudianteId")
	List<Recomendacion> buscarPorEstudiante(@Param("estudianteId") Long estudianteId);

}
