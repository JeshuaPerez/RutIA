package com.rutaia.entidad;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "estudiante")
public class Estudiante {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre_completo", nullable = false, length = 120)
	private String nombreCompleto;

	@Column(name = "correo", nullable = false, length = 254, unique = true)
	private String correo;

	@Column(name = "nivel_experiencia", nullable = false, length = 20)
	private NivelExperiencia nivelExperiencia;

	@Column(name = "area_interes", nullable = false, length = 100)
	private String areaInteres;

	@Column(name = "fecha_registro", nullable = false, updatable = false)
	private LocalDateTime fechaRegistro;

	protected Estudiante() {
	}

	public Estudiante(String nombreCompleto, String correo, NivelExperiencia nivelExperiencia, String areaInteres) {
		this.nombreCompleto = nombreCompleto;
		this.correo = correo;
		this.nivelExperiencia = nivelExperiencia;
		this.areaInteres = areaInteres;
	}

	// La fecha se toma en la hora local de la API (C6), no en la del contenedor de PostgreSQL.
	@PrePersist
	void asignarFechaRegistro() {
		fechaRegistro = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	}

	public Long getId() {
		return id;
	}

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public String getCorreo() {
		return correo;
	}

	public NivelExperiencia getNivelExperiencia() {
		return nivelExperiencia;
	}

	public String getAreaInteres() {
		return areaInteres;
	}

	public LocalDateTime getFechaRegistro() {
		return fechaRegistro;
	}

}
