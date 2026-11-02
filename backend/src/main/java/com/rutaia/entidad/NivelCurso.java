package com.rutaia.entidad;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Nivel del curso (C5): el texto exacto es el que viaja en la BD y en el JSON. */
public enum NivelCurso {

	BASICO("Básico"),
	INTERMEDIO("Intermedio"),
	AVANZADO("Avanzado");

	public static final String VALORES = "Básico, Intermedio o Avanzado";

	private final String texto;

	NivelCurso(String texto) {
		this.texto = texto;
	}

	@JsonValue
	public String getTexto() {
		return texto;
	}

	@JsonCreator
	public static NivelCurso desdeTexto(String texto) {
		return Arrays.stream(values())
				.filter(nivel -> nivel.texto.equals(texto))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("debe ser " + VALORES));
	}

}
