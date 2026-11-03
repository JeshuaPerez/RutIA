package com.rutaia.dominio;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Nivel del estudiante (C5): el texto exacto es el que viaja en la BD y en el JSON. */
public enum NivelExperiencia {

	PRINCIPIANTE("Principiante"),
	INTERMEDIO("Intermedio"),
	AVANZADO("Avanzado");

	public static final String VALORES = "Principiante, Intermedio o Avanzado";

	private final String texto;

	NivelExperiencia(String texto) {
		this.texto = texto;
	}

	@JsonValue
	public String getTexto() {
		return texto;
	}

	@JsonCreator
	public static NivelExperiencia desdeTexto(String texto) {
		return Arrays.stream(values())
				.filter(nivel -> nivel.texto.equals(texto))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("debe ser " + VALORES));
	}

}
