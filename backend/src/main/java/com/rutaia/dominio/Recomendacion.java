package com.rutaia.dominio;

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

/** Solo existe para las consultas que terminaron Respondida (relación 1–0..1 de C4). */
@Entity
@Table(name = "recomendacion")
public class Recomendacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "consulta_id", nullable = false, unique = true, updatable = false)
	private Consulta consulta;

	@Column(name = "respuesta", nullable = false, columnDefinition = "text")
	private String respuesta;

	@Column(name = "fecha", nullable = false, updatable = false)
	private LocalDateTime fecha;

	protected Recomendacion() {
	}

	public Recomendacion(Consulta consulta, String respuesta) {
		this.consulta = consulta;
		this.respuesta = respuesta;
	}

	@PrePersist
	void asignarFecha() {
		fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	}

	public Long getId() {
		return id;
	}

	public Consulta getConsulta() {
		return consulta;
	}

	public String getRespuesta() {
		return respuesta;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

}
