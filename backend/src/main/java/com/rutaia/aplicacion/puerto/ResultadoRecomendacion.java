package com.rutaia.aplicacion.puerto;

import java.util.List;

import com.rutaia.dominio.EstadoConsulta;

/**
 * Resultado de {@link MotorRecomendacion}: Respondida, con la respuesta y sus fuentes, o Sin resultados. De cada
 * fuente solo se toman el cursoId y la similitud; los datos del curso se leen de PostgreSQL al guardar.
 */
public record ResultadoRecomendacion(
		EstadoConsulta estado,
		String respuesta,
		List<Fuente> fuentes) {

	public ResultadoRecomendacion {
		if (estado != EstadoConsulta.RESPONDIDA && estado != EstadoConsulta.SIN_RESULTADOS) {
			throw new IllegalArgumentException("El motor solo devuelve Respondida o Sin resultados: " + estado);
		}
		fuentes = fuentes == null ? List.of() : List.copyOf(fuentes);
	}

	public static ResultadoRecomendacion respondida(String respuesta, List<Fuente> fuentes) {
		return new ResultadoRecomendacion(EstadoConsulta.RESPONDIDA, respuesta, fuentes);
	}

	public static ResultadoRecomendacion sinResultados() {
		return new ResultadoRecomendacion(EstadoConsulta.SIN_RESULTADOS, null, List.of());
	}

	public boolean esRespondida() {
		return estado == EstadoConsulta.RESPONDIDA;
	}

	public record Fuente(Long cursoId, double similitud) {
	}

}
