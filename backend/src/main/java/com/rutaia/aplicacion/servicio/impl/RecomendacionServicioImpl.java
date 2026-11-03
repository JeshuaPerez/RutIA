package com.rutaia.aplicacion.servicio.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.aplicacion.excepcion.RespuestaMotorInvalidaException;
import com.rutaia.aplicacion.puerto.ResultadoRecomendacion;
import com.rutaia.aplicacion.servicio.RecomendacionServicio;
import com.rutaia.dominio.Consulta;
import com.rutaia.dominio.Curso;
import com.rutaia.dominio.Recomendacion;
import com.rutaia.dominio.RecomendacionFuente;
import com.rutaia.persistencia.CursoRepositorio;
import com.rutaia.persistencia.RecomendacionFuenteRepositorio;
import com.rutaia.persistencia.RecomendacionRepositorio;

@Service
public class RecomendacionServicioImpl implements RecomendacionServicio {

	private final CursoRepositorio cursoRepositorio;
	private final RecomendacionRepositorio recomendacionRepositorio;
	private final RecomendacionFuenteRepositorio recomendacionFuenteRepositorio;

	public RecomendacionServicioImpl(CursoRepositorio cursoRepositorio, RecomendacionRepositorio recomendacionRepositorio,
			RecomendacionFuenteRepositorio recomendacionFuenteRepositorio) {
		this.cursoRepositorio = cursoRepositorio;
		this.recomendacionRepositorio = recomendacionRepositorio;
		this.recomendacionFuenteRepositorio = recomendacionFuenteRepositorio;
	}

	@Override
	@Transactional
	public RecomendacionConFuentes guardar(Consulta consulta, ResultadoRecomendacion resultado) {
		List<ResultadoRecomendacion.Fuente> fuentesMotor = new ArrayList<>(resultado.fuentes());
		fuentesMotor.sort(Comparator.comparingDouble(ResultadoRecomendacion.Fuente::similitud).reversed());

		Map<Long, Curso> cursos = cursoRepositorio.findAllById(
				fuentesMotor.stream().map(ResultadoRecomendacion.Fuente::cursoId).toList()).stream()
				.collect(Collectors.toMap(Curso::getId, Function.identity()));
		for (ResultadoRecomendacion.Fuente fuente : fuentesMotor) {
			Curso curso = cursos.get(fuente.cursoId());
			if (curso == null) {
				throw new RespuestaMotorInvalidaException(
						"La fuente con cursoId " + fuente.cursoId() + " no existe en PostgreSQL");
			}
			if (!curso.getActivo()) {
				throw new RespuestaMotorInvalidaException(
						"La fuente con cursoId " + fuente.cursoId() + " está inactiva en PostgreSQL");
			}
		}

		Recomendacion recomendacion = recomendacionRepositorio.save(
				new Recomendacion(consulta, resultado.respuesta().strip()));
		List<RecomendacionFuente> fuentes = new ArrayList<>();
		for (ResultadoRecomendacion.Fuente fuente : fuentesMotor) {
			fuentes.add(recomendacionFuenteRepositorio.save(new RecomendacionFuente(
					recomendacion, cursos.get(fuente.cursoId()), redondear(fuente.similitud()))));
		}
		return new RecomendacionConFuentes(recomendacion, fuentes);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Recomendacion> buscar(Long recomendacionId) {
		return recomendacionRepositorio.findById(recomendacionId);
	}

	/** Dos consultas: la recomendación y sus fuentes con su curso (JOIN FETCH). */
	@Override
	@Transactional(readOnly = true)
	public Optional<RecomendacionConFuentes> buscarPorConsulta(Long consultaId) {
		return recomendacionRepositorio.findByConsultaId(consultaId)
				.map(recomendacion -> new RecomendacionConFuentes(recomendacion,
						recomendacionFuenteRepositorio.buscarPorRecomendacion(recomendacion.getId())));
	}

	/** Similitud con 4 decimales (C7). */
	private static double redondear(double similitud) {
		return Math.round(similitud * 10_000) / 10_000.0;
	}

}
