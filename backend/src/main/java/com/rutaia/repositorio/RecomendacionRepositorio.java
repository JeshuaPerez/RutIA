package com.rutaia.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rutaia.entidad.Recomendacion;

public interface RecomendacionRepositorio extends JpaRepository<Recomendacion, Long> {
}
