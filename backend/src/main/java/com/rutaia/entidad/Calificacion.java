package com.rutaia.entidad;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/** Calificación de una recomendación (RF 17): a lo sumo una por recomendación (uq_calificacion_recomendacion). */
@Entity
@Table(name = "calificacion")
public class Calificacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recomendacion_id", nullable = false, unique = true, updatable = false)
	private Recomendacion recomendacion;

	@Column(name = "puntuacion", nullable = false, updatable = false)
	private Integer puntuacion;

	@Column(name = "comentario", length = 500, updatable = false)
	private String comentario;

	@Column(name = "fecha", nullable = false, updatable = false)
	private LocalDateTime fecha;

	protected Calificacion() {
	}

	public Calificacion(Recomendacion recomendacion, Integer puntuacion, String comentario) {
		this.recomendacion = recomendacion;
		this.puntuacion = puntuacion;
		this.comentario = comentario;
	}

	// La fecha se toma en la hora local de la API (C6), no en la del contenedor de PostgreSQL.
	@PrePersist
	void asignarFecha() {
		fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	}

	public Long getId() {
		return id;
	}

	public Recomendacion getRecomendacion() {
		return recomendacion;
	}

	public Integer getPuntuacion() {
		return puntuacion;
	}

	public String getComentario() {
		return comentario;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

}
