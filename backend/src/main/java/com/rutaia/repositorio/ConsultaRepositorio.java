package com.rutaia.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rutaia.entidad.Consulta;

public interface ConsultaRepositorio extends JpaRepository<Consulta, Long> {
}
