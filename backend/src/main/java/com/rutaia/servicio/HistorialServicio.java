package com.rutaia.servicio;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.dto.ConsultaDetalle;
import com.rutaia.dto.ConsultaResultado;
import com.rutaia.dto.FuenteResponse;
import com.rutaia.dto.HistorialItem;
import com.rutaia.entidad.Calificacion;
import com.rutaia.entidad.Consulta;
import com.rutaia.entidad.EstadoConsulta;
import com.rutaia.entidad.Recomendacion;
import com.rutaia.entidad.RecomendacionFuente;
import com.rutaia.excepcion.RecursoNoEncontradoException;
import com.rutaia.repositorio.CalificacionRepositorio;
import com.rutaia.repositorio.ConsultaRepositorio;
import com.rutaia.repositorio.EstudianteRepositorio;
import com.rutaia.repositorio.RecomendacionFuenteRepositorio;
import com.rutaia.repositorio.RecomendacionRepositorio;

/** E-04 (historial de un estudiante) y E-12 (detalle de una consulta). Solo lectura. */
@Service
public class HistorialServicio {

	private final EstudianteRepositorio estudianteRepositorio;
	private final ConsultaRepositorio consultaRepositorio;
	private final RecomendacionRepositorio recomendacionRepositorio;
	private final RecomendacionFuenteRepositorio recomendacionFuenteRepositorio;
	private final CalificacionRepositorio calificacionRepositorio;

	public HistorialServicio(EstudianteRepositorio estudianteRepositorio, ConsultaRepositorio consultaRepositorio,
			RecomendacionRepositorio recomendacionRepositorio,
			RecomendacionFuenteRepositorio recomendacionFuenteRepositorio,
			CalificacionRepositorio calificacionRepositorio) {
		this.estudianteRepositorio = estudianteRepositorio;
		this.consultaRepositorio = consultaRepositorio;
		this.recomendacionRepositorio = recomendacionRepositorio;
		this.recomendacionFuenteRepositorio = recomendacionFuenteRepositorio;
		this.calificacionRepositorio = calificacionRepositorio;
	}

	/**
	 * Consultas del estudiante por fecha descendente. Hace tres consultas sin importar cuántos registros haya: las
	 * consultas, sus recomendaciones y las fuentes con su curso (JOIN FETCH).
	 */
	@Transactional(readOnly = true)
	public List<HistorialItem> historial(Long estudianteId) {
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
					return HistorialItem.desde(consulta, recomendacion, fuentes);
				})
				.toList();
	}

	/** Arma el ConsultaResultado igual que E-11 (mismas fábricas) y le agrega la calificación. */
	@Transactional(readOnly = true)
	public ConsultaDetalle detalle(Long consultaId) {
		Consulta consulta = consultaRepositorio.findById(consultaId)
				.orElseThrow(() -> new RecursoNoEncontradoException("No existe una consulta con id " + consultaId));
		if (consulta.getEstado() != EstadoConsulta.RESPONDIDA) {
			return ConsultaDetalle.desde(ConsultaResultado.sinRecomendacion(consulta), null);
		}
		Recomendacion recomendacion = recomendacionRepositorio.findByConsultaId(consultaId)
				.orElseThrow(() -> sinRecomendacion(consulta));
		List<FuenteResponse> fuentes = recomendacionFuenteRepositorio.buscarPorRecomendacion(recomendacion.getId())
				.stream()
				.map(FuenteResponse::desde)
				.toList();
		Calificacion calificacion = calificacionRepositorio.findByRecomendacionId(recomendacion.getId()).orElse(null);
		return ConsultaDetalle.desde(ConsultaResultado.respondida(consulta, recomendacion, fuentes), calificacion);
	}

	/** Dato inconsistente (C4 exige la recomendación de toda Respondida): se responde 500 y queda en el log. */
	private static IllegalStateException sinRecomendacion(Consulta consulta) {
		return new IllegalStateException("La consulta " + consulta.getId() + " está Respondida pero no tiene recomendación");
	}

}
