package com.rutaia.aplicacion.servicio;

import java.util.List;
import java.util.Optional;

import com.rutaia.aplicacion.puerto.ResultadoRecomendacion;
import com.rutaia.dominio.Consulta;
import com.rutaia.dominio.Recomendacion;
import com.rutaia.dominio.RecomendacionFuente;

/**
 * Recomendaciones y sus fuentes. Lo usan otros servicios de la capa de aplicación (consultas, historial y
 * calificaciones), nunca un controlador; por eso trabaja con entidades.
 */
public interface RecomendacionServicio {

	/**
	 * Guarda la recomendación de una consulta Respondida y sus fuentes por similitud descendente, con la similitud
	 * redondeada a 4 decimales (C7). Los datos de cada fuente se leen de PostgreSQL; del motor solo se toman el
	 * cursoId y la similitud. Participa en la transacción de quien lo llama.
	 *
	 * @throws com.rutaia.aplicacion.excepcion.RespuestaMotorInvalidaException si alguna fuente no existe o está
	 *         inactiva; no se escribe nada
	 */
	RecomendacionConFuentes guardar(Consulta consulta, ResultadoRecomendacion resultado);

	/** La recomendación con ese id; vacío si no existe. */
	Optional<Recomendacion> buscar(Long recomendacionId);

	/** La recomendación de la consulta con sus fuentes por similitud descendente; vacío si la consulta no tiene. */
	Optional<RecomendacionConFuentes> buscarPorConsulta(Long consultaId);

	/** Recomendación con sus fuentes por similitud descendente, cada una con su curso cargado. */
	record RecomendacionConFuentes(Recomendacion recomendacion, List<RecomendacionFuente> fuentes) {
	}

}
