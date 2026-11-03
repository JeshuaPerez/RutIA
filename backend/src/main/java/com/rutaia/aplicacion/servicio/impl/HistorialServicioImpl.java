package com.rutaia.aplicacion.servicio.impl;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.aplicacion.dto.consulta.ConsultaDetalleResponse;
import com.rutaia.aplicacion.dto.consulta.HistorialItemResponse;
import com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException;
import com.rutaia.aplicacion.mapper.ConsultaMapper;
import com.rutaia.aplicacion.servicio.HistorialServicio;
import com.rutaia.aplicacion.servicio.RecomendacionServicio;
import com.rutaia.aplicacion.servicio.RecomendacionServicio.RecomendacionConFuentes;
import com.rutaia.dominio.Calificacion;
import com.rutaia.dominio.Consulta;
import com.rutaia.dominio.EstadoConsulta;
import com.rutaia.dominio.Recomendacion;
import com.rutaia.dominio.RecomendacionFuente;
import com.rutaia.persistencia.CalificacionRepositorio;
import com.rutaia.persistencia.ConsultaRepositorio;
import com.rutaia.persistencia.EstudianteRepositorio;
import com.rutaia.persistencia.RecomendacionFuenteRepositorio;
import com.rutaia.persistencia.RecomendacionRepositorio;

/** E-04 (historial de un estudiante) y E-12 (detalle de una consulta). Solo lectura. */
@Service
public class HistorialServicioImpl implements HistorialServicio {

	private final EstudianteRepositorio estudianteRepositorio;
	private final ConsultaRepositorio consultaRepositorio;
	private final RecomendacionRepositorio recomendacionRepositorio;
	private final RecomendacionFuenteRepositorio recomendacionFuenteRepositorio;
	private final CalificacionRepositorio calificacionRepositorio;
	private final RecomendacionServicio recomendacionServicio;

	public HistorialServicioImpl(EstudianteRepositorio estudianteRepositorio, ConsultaRepositorio consultaRepositorio,
			RecomendacionRepositorio recomendacionRepositorio,
			RecomendacionFuenteRepositorio recomendacionFuenteRepositorio,
			CalificacionRepositorio calificacionRepositorio, RecomendacionServicio recomendacionServicio) {
		this.estudianteRepositorio = estudianteRepositorio;
		this.consultaRepositorio = consultaRepositorio;
		this.recomendacionRepositorio = recomendacionRepositorio;
		this.recomendacionFuenteRepositorio = recomendacionFuenteRepositorio;
		this.calificacionRepositorio = calificacionRepositorio;
		this.recomendacionServicio = recomendacionServicio;
	}

	/**
	 * Consultas del estudiante por fecha descendente. Hace tres consultas sin importar cuántos registros haya: las
	 * consultas, sus recomendaciones y las fuentes con su curso (JOIN FETCH).
	 */
	@Override
	@Transactional(readOnly = true)
	public List<HistorialItemResponse> historial(Long estudianteId) {
		if (!estudianteRepositorio.existsById(estudianteId)) {
			throw new RecursoNoEncontradoException("No existe un estudiante con id " + estudianteId);
		}
		List<Consulta> consultas = consultaRepositorio.buscarHistorial(estudianteId);
		if (consultas.isEmpty()) {
			return List.of();
		}
		Map<Long, Recomendacion> recomendacionPorConsulta = recomendacionRepositorio.buscarPorEstudiante(estudianteId)
				.stream()
				.collect(Collectors.toMap(r -> r.getConsulta().getId(), Function.identity()));
		// groupingBy conserva el orden por similitud de la consulta dentro de cada lista.
		Map<Long, List<RecomendacionFuente>> fuentesPorRecomendacion = recomendacionFuenteRepositorio
				.buscarPorEstudiante(estudianteId).stream()
				.collect(Collectors.groupingBy(f -> f.getRecomendacion().getId()));

		return consultas.stream()
				.map(consulta -> {
					Recomendacion recomendacion = recomendacionPorConsulta.get(consulta.getId());
					if (consulta.getEstado() == EstadoConsulta.RESPONDIDA && recomendacion == null) {
						throw sinRecomendacion(consulta);
					}
					List<RecomendacionFuente> fuentes = recomendacion == null
							? List.of()
							: fuentesPorRecomendacion.getOrDefault(recomendacion.getId(), List.of());
					return ConsultaMapper.aHistorialItem(consulta, recomendacion, fuentes);
				})
				.toList();
	}

	/** Arma el ConsultaResultadoResponse igual que E-11 (mismas fábricas) y le agrega la calificación. */
	@Override
	@Transactional(readOnly = true)
	public ConsultaDetalleResponse detalle(Long consultaId) {
		Consulta consulta = consultaRepositorio.findById(consultaId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una consulta con id " + consultaId));
		if (consulta.getEstado() != EstadoConsulta.RESPONDIDA) {
			return ConsultaMapper.aDetalle(ConsultaMapper.sinRecomendacion(consulta), null);
		}
		RecomendacionConFuentes recomendacion = recomendacionServicio.buscarPorConsulta(consultaId)
				.orElseThrow(() -> sinRecomendacion(consulta));
		Calificacion calificacion = calificacionRepositorio
				.findByRecomendacionId(recomendacion.recomendacion().getId()).orElse(null);
		return ConsultaMapper.aDetalle(
				ConsultaMapper.respondida(consulta, recomendacion.recomendacion(), recomendacion.fuentes()), calificacion);
	}

	/** Dato inconsistente (C4 exige la recomendación de toda Respondida): se responde 500 y queda en el log. */
	private static IllegalStateException sinRecomendacion(Consulta consulta) {
		return new IllegalStateException("La consulta " + consulta.getId() + " está Respondida pero no tiene recomendación");
	}

}
