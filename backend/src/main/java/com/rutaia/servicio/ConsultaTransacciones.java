package com.rutaia.servicio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.dto.ConsultaRequest;
import com.rutaia.dto.ConsultaResultado;
import com.rutaia.dto.FuenteResponse;
import com.rutaia.entidad.Consulta;
import com.rutaia.entidad.Curso;
import com.rutaia.entidad.Estudiante;
import com.rutaia.entidad.Recomendacion;
import com.rutaia.entidad.RecomendacionFuente;
import com.rutaia.excepcion.RecursoNoEncontradoException;
import com.rutaia.integracion.N8nPeticion;
import com.rutaia.integracion.N8nRespuesta;
import com.rutaia.repositorio.ConsultaRepositorio;
import com.rutaia.repositorio.CursoRepositorio;
import com.rutaia.repositorio.EstudianteRepositorio;
import com.rutaia.repositorio.RecomendacionFuenteRepositorio;
import com.rutaia.repositorio.RecomendacionRepositorio;

/**
 * Transacciones cortas de E-11. Está separada de {@link ConsultaServicio} para que cada método pase por el proxy de
 * Spring (una transacción propia que se confirma al volver) y la llamada a n8n quede fuera de toda transacción.
 */
@Service
public class ConsultaTransacciones {

	private final EstudianteRepositorio estudianteRepositorio;
	private final ConsultaRepositorio consultaRepositorio;
	private final CursoRepositorio cursoRepositorio;
	private final RecomendacionRepositorio recomendacionRepositorio;
	private final RecomendacionFuenteRepositorio recomendacionFuenteRepositorio;

	public ConsultaTransacciones(EstudianteRepositorio estudianteRepositorio, ConsultaRepositorio consultaRepositorio,
			CursoRepositorio cursoRepositorio, RecomendacionRepositorio recomendacionRepositorio,
			RecomendacionFuenteRepositorio recomendacionFuenteRepositorio) {
		this.estudianteRepositorio = estudianteRepositorio;
		this.consultaRepositorio = consultaRepositorio;
		this.cursoRepositorio = cursoRepositorio;
		this.recomendacionRepositorio = recomendacionRepositorio;
		this.recomendacionFuenteRepositorio = recomendacionFuenteRepositorio;
	}

	/**
	 * Pasos 2 y 3 de E-11: comprueba el estudiante (404 sin crear nada) e inserta la consulta en Pendiente. Al volver
	 * la transacción ya está confirmada. Devuelve la petición para W-1 con el nivel y el área del estudiante (RF 08).
	 */
	@Transactional
	public N8nPeticion registrarPendiente(ConsultaRequest solicitud) {
		Estudiante estudiante = estudianteRepositorio.findById(solicitud.estudianteId())
				.orElseThrow(() -> new RecursoNoEncontradoException(
						"No existe un estudiante con id " + solicitud.estudianteId()));
		Consulta consulta = consultaRepositorio.save(new Consulta(estudiante, solicitud.pregunta()));
		return new N8nPeticion(consulta.getId(), consulta.getPregunta(), estudiante.getNivelExperiencia(),
				estudiante.getAreaInteres());
	}

	/**
	 * Guarda la recomendación y sus fuentes y pasa la consulta a Respondida, todo en la misma transacción. Los datos
	 * de cada fuente se leen de PostgreSQL; de n8n solo se toman el cursoId y la similitud.
	 *
	 * @throws RespuestaN8nInvalidaException si alguna fuente no existe o está inactiva; no se escribe nada
	 */
	@Transactional
	public ConsultaResultado guardarRespondida(Long consultaId, N8nRespuesta respuesta) {
		Consulta consulta = buscar(consultaId);
		List<N8nRespuesta.Fuente> fuentesN8n = new ArrayList<>(respuesta.fuentes());
		fuentesN8n.sort(Comparator.comparing(N8nRespuesta.Fuente::similitud).reversed());

		Map<Long, Curso> cursos = cursoRepositorio.findAllById(
				fuentesN8n.stream().map(N8nRespuesta.Fuente::cursoId).toList()).stream()
				.collect(Collectors.toMap(Curso::getId, Function.identity()));
		for (N8nRespuesta.Fuente fuente : fuentesN8n) {
			Curso curso = cursos.get(fuente.cursoId());
			if (curso == null) {
				throw new RespuestaN8nInvalidaException(
						"La fuente con cursoId " + fuente.cursoId() + " no existe en PostgreSQL");
			}
			if (!curso.getActivo()) {
				throw new RespuestaN8nInvalidaException(
						"La fuente con cursoId " + fuente.cursoId() + " está inactiva en PostgreSQL");
			}
		}

		Recomendacion recomendacion = recomendacionRepositorio.save(
				new Recomendacion(consulta, respuesta.respuesta().strip()));
		List<FuenteResponse> fuentes = new ArrayList<>();
		for (N8nRespuesta.Fuente fuente : fuentesN8n) {
			RecomendacionFuente guardada = recomendacionFuenteRepositorio.save(new RecomendacionFuente(
					recomendacion, cursos.get(fuente.cursoId()), redondear(fuente.similitud())));
			fuentes.add(FuenteResponse.desde(guardada));
		}
		consulta.marcarRespondida();
		return ConsultaResultado.respondida(consulta, recomendacion, fuentes);
	}

	@Transactional
	public ConsultaResultado guardarSinResultados(Long consultaId) {
		Consulta consulta = buscar(consultaId);
		consulta.marcarSinResultados();
		return ConsultaResultado.sinRecomendacion(consulta);
	}

	@Transactional
	public ConsultaResultado guardarError(Long consultaId, String detalleError) {
		Consulta consulta = buscar(consultaId);
		consulta.marcarError(detalleError);
		return ConsultaResultado.sinRecomendacion(consulta);
	}

	private Consulta buscar(Long consultaId) {
		return consultaRepositorio.findById(consultaId)
				.orElseThrow(() -> new IllegalStateException("No existe la consulta " + consultaId));
	}

	/** Similitud con 4 decimales (C7). */
	private static double redondear(double similitud) {
		return Math.round(similitud * 10_000) / 10_000.0;
	}

}
