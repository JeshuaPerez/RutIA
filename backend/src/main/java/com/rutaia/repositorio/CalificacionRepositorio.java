package com.rutaia.repositorio;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rutaia.entidad.Calificacion;

public interface CalificacionRepositorio extends JpaRepository<Calificacion, Long> {

	boolean existsByRecomendacionId(Long recomendacionId);

	Optional<Calificacion> findByRecomendacionId(Long recomendacionId);

	/** Promedio de todas las puntuaciones (RF 18); null si no hay calificaciones. */
	@Query("select avg(c.puntuacion) from Calificacion c")
	Double promediarPuntuaciones();

}
