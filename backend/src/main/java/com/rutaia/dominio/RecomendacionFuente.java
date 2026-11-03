package com.rutaia.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Curso usado como fuente de una recomendación, con la similitud que devolvió Qdrant (RF 14). */
@Entity
@Table(name = "recomendacion_fuente")
public class RecomendacionFuente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recomendacion_id", nullable = false, updatable = false)
	private Recomendacion recomendacion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "curso_id", nullable = false, updatable = false)
	private Curso curso;

	@Column(name = "similitud", nullable = false)
	private Double similitud;

	protected RecomendacionFuente() {
	}

	public RecomendacionFuente(Recomendacion recomendacion, Curso curso, Double similitud) {
		this.recomendacion = recomendacion;
		this.curso = curso;
		this.similitud = similitud;
	}

	public Long getId() {
		return id;
	}

	public Recomendacion getRecomendacion() {
		return recomendacion;
	}

	public Curso getCurso() {
		return curso;
	}

	public Double getSimilitud() {
		return similitud;
	}

}
