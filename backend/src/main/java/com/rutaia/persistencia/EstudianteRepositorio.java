package com.rutaia.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rutaia.dominio.Estudiante;

public interface EstudianteRepositorio extends JpaRepository<Estudiante, Long> {

	boolean existsByCorreo(String correo);

	List<Estudiante> findAllByOrderByIdAsc();

}
