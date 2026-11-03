package com.rutaia.entidad;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Estado de la consulta (C5): el texto exacto es el que viaja en la BD y en el JSON. Solo Spring Boot escribe
 * estados; las transiciones válidas son Pendiente → Respondida | Sin resultados | Error.
 */
public enum EstadoConsulta {

	PENDIENTE("Pendiente"),
	RESPONDIDA("Respondida"),
	SIN_RESULTADOS("Sin resultados"),
	ERROR("Error");

	public static final String VALORES = "Pendiente, Respondida, Sin resultados o Error";

	private final String texto;

	EstadoConsulta(String texto) {
		this.texto = texto;
	}

	@JsonValue
	public String getTexto() {
		return texto;
	}

	@JsonCreator
	public static EstadoConsulta desdeTexto(String texto) {
		return Arrays.stream(values())
				.filter(estado -> estado.texto.equals(texto))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("debe ser " + VALORES));
	}

}
