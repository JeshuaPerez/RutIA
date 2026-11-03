package com.rutaia.aplicacion.servicio.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.aplicacion.dto.consulta.ConsultaRequest;
import com.rutaia.aplicacion.dto.consulta.ConsultaResultadoResponse;
import com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException;
import com.rutaia.aplicacion.mapper.ConsultaMapper;
import com.rutaia.aplicacion.puerto.ResultadoRecomendacion;
import com.rutaia.aplicacion.puerto.SolicitudRecomendacion;
import com.rutaia.aplicacion.servicio.RecomendacionServicio;
import com.rutaia.aplicacion.servicio.RecomendacionServicio.RecomendacionConFuentes;
import com.rutaia.dominio.Consulta;
import com.rutaia.dominio.Estudiante;
import com.rutaia.persistencia.ConsultaRepositorio;
import com.rutaia.persistencia.EstudianteRepositorio;

/**
 * Transacciones cortas de E-11. Está separada de {@link ConsultaServicioImpl} para que cada método pase por el proxy
 * de Spring (una transacción propia que se confirma al volver) y la llamada al motor quede fuera de toda transacción.
 */
@Service
public class ConsultaTransacciones {

	private final EstudianteRepositorio estudianteRepositorio;
	private final ConsultaRepositorio consultaRepositorio;
	private final RecomendacionServicio recomendacionServicio;

	public ConsultaTransacciones(EstudianteRepositorio estudianteRepositorio, ConsultaRepositorio consultaRepositorio,
			RecomendacionServicio recomendacionServicio) {
		this.estudianteRepositorio = estudianteRepositorio;
		this.consultaRepositorio = consultaRepositorio;
		this.recomendacionServicio = recomendacionServicio;
	}

	/**
	 * Pasos 2 y 3 de E-11: comprueba el estudiante (404 sin crear nada) e inserta la consulta en Pendiente. Al volver
	 * la transacción ya está confirmada. Devuelve la solicitud para el motor con el nivel y el área del estudiante
	 * (RF 08).
	 */
	@Transactional
	public SolicitudRecomendacion registrarPendiente(ConsultaRequest solicitud) {
		Estudiante estudiante = estudianteRepositorio.findById(solicitud.estudianteId())
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"No existe un estudiante con id " + solicitud.estudianteId()));
		Consulta consulta = consultaRepositorio.save(new Consulta(estudiante, solicitud.pregunta()));
		return new SolicitudRecomendacion(consulta.getId(), consulta.getPregunta(), estudiante.getNivelExperiencia(),
				estudiante.getAreaInteres());
	}

	/**
	 * Guarda la recomendación y sus fuentes y pasa la consulta a Respondida, todo en la misma transacción.
	 *
	 * @throws com.rutaia.aplicacion.excepcion.RespuestaMotorInvalidaException si alguna fuente no existe o está
	 *         inactiva; no se escribe nada
	 */
	@Transactional
	public ConsultaResultadoResponse guardarRespondida(Long consultaId, ResultadoRecomendacion resultado) {
		Consulta consulta = buscar(consultaId);
		RecomendacionConFuentes guardada = recomendacionServicio.guardar(consulta, resultado);
		consulta.marcarRespondida();
		return ConsultaMapper.respondida(consulta, guardada.recomendacion(), guardada.fuentes());
	}

	@Transactional
	public ConsultaResultadoResponse guardarSinResultados(Long consultaId) {
		Consulta consulta = buscar(consultaId);
		consulta.marcarSinResultados();
		return ConsultaMapper.sinRecomendacion(consulta);
	}

	@Transactional
	public ConsultaResultadoResponse guardarError(Long consultaId, String detalleError) {
		Consulta consulta = buscar(consultaId);
		consulta.marcarError(detalleError);
		return ConsultaMapper.sinRecomendacion(consulta);
	}

	private Consulta buscar(Long consultaId) {
		return consultaRepositorio.findById(consultaId)
				.orElseThrow(() -> new IllegalStateException("No existe la consulta " + consultaId));
	}

}
