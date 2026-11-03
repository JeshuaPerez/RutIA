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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "consulta")
public class Consulta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "estudiante_id", nullable = false, updatable = false)
	private Estudiante estudiante;

	@Column(name = "pregunta", nullable = false, length = 500, updatable = false)
	private String pregunta;

	@Column(name = "fecha", nullable = false, updatable = false)
	private LocalDateTime fecha;

	@Column(name = "estado", nullable = false, length = 20)
	private EstadoConsulta estado = EstadoConsulta.PENDIENTE;

	@Column(name = "detalle_error", columnDefinition = "text")
	private String detalleError;

	protected Consulta() {
	}

	public Consulta(Estudiante estudiante, String pregunta) {
		this.estudiante = estudiante;
		this.pregunta = pregunta;
	}

	// La fecha se toma en la hora local de la API (C6), no en la del contenedor de PostgreSQL.
	@PrePersist
	void asignarFecha() {
		fecha = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
	}

	public void marcarRespondida() {
		finalizar(EstadoConsulta.RESPONDIDA, null);
	}

	public void marcarSinResultados() {
		finalizar(EstadoConsulta.SIN_RESULTADOS, null);
	}

	/** El detalle técnico queda solo en la BD y en los logs; la API nunca lo devuelve. */
	public void marcarError(String detalleError) {
		finalizar(EstadoConsulta.ERROR, detalleError);
	}

	/** Pendiente es el único estado que admite transición; los otros tres son finales (C5). */
	private void finalizar(EstadoConsulta estadoFinal, String detalle) {
		if (estado != EstadoConsulta.PENDIENTE) {
			throw new IllegalStateException("La consulta " + id + " ya está en el estado final " + estado.getTexto());
		}
		this.estado = estadoFinal;
		this.detalleError = detalle;
	}

	public Long getId() {
		return id;
	}

	public Estudiante getEstudiante() {
		return estudiante;
	}

	public String getPregunta() {
		return pregunta;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public EstadoConsulta getEstado() {
		return estado;
	}

	public String getDetalleError() {
		return detalleError;
	}

}
