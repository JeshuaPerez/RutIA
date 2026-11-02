package com.rutaia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "curso")
public class Curso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", nullable = false, length = 150)
	private String nombre;

	@Column(name = "descripcion", nullable = false, columnDefinition = "text")
	private String descripcion;

	@Column(name = "categoria", nullable = false, length = 80)
	private String categoria;

	@Column(name = "nivel", nullable = false, length = 20)
	private NivelCurso nivel;

	@Column(name = "duracion_horas", nullable = false)
	private Integer duracionHoras;

	@Column(name = "activo", nullable = false)
	private Boolean activo = Boolean.TRUE;

	protected Curso() {
	}

	public Curso(String nombre, String descripcion, String categoria, NivelCurso nivel, Integer duracionHoras) {
		actualizar(nombre, descripcion, categoria, nivel, duracionHoras);
	}

	/** Reemplaza los datos descriptivos; el estado activo no cambia. */
	public void actualizar(String nombre, String descripcion, String categoria, NivelCurso nivel, Integer duracionHoras) {
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.categoria = categoria;
		this.nivel = nivel;
		this.duracionHoras = duracionHoras;
	}

	public void desactivar() {
		this.activo = Boolean.FALSE;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public String getCategoria() {
		return categoria;
	}

	public NivelCurso getNivel() {
		return nivel;
	}

	public Integer getDuracionHoras() {
		return duracionHoras;
	}

	public Boolean getActivo() {
		return activo;
	}

}
