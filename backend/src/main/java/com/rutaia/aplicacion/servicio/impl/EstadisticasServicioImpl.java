package com.rutaia.aplicacion.servicio.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rutaia.aplicacion.dto.estadistica.EstadisticasResponse;
import com.rutaia.aplicacion.servicio.EstadisticasServicio;
import com.rutaia.dominio.EstadoConsulta;
import com.rutaia.persistencia.CalificacionRepositorio;
import com.rutaia.persistencia.ConsultaRepositorio;
import com.rutaia.persistencia.RecomendacionFuenteRepositorio;

/** E-14 (RF 18): todo se calcula con consultas agregadas; no se cargan entidades. */
@Service
public class EstadisticasServicioImpl implements EstadisticasServicio {

	private final ConsultaRepositorio consultaRepositorio;
	private final CalificacionRepositorio calificacionRepositorio;
	private final RecomendacionFuenteRepositorio recomendacionFuenteRepositorio;

	public EstadisticasServicioImpl(ConsultaRepositorio consultaRepositorio, CalificacionRepositorio calificacionRepositorio,
			RecomendacionFuenteRepositorio recomendacionFuenteRepositorio) {
		this.consultaRepositorio = consultaRepositorio;
		this.calificacionRepositorio = calificacionRepositorio;
		this.recomendacionFuenteRepositorio = recomendacionFuenteRepositorio;
	}

	@Override
	@Transactional(readOnly = true)
	public EstadisticasResponse calcular() {
		ConsultaRepositorio.ConteoConsultas conteo = consultaRepositorio.contarPorEstado(
				EstadoConsulta.RESPONDIDA, EstadoConsulta.SIN_RESULTADOS);
		Double promedio = calificacionRepositorio.promediarPuntuaciones();
		EstadisticasResponse.CursoMasRecomendado curso = recomendacionFuenteRepositorio
				.buscarMasRecomendados(Limit.of(1)).stream()
				.findFirst()
				.map(fila -> new EstadisticasResponse.CursoMasRecomendado(fila.getCursoId(), fila.getNombre(),
						fila.getVecesRecomendado()))
				.orElse(null);
		return new EstadisticasResponse(
				conteo.getTotal(),
				conteo.getRespondidas(),
				conteo.getSinResultados(),
				promedio == null ? null : redondear(promedio),
				curso);
	}

	/** 2 decimales, mitad hacia arriba, igual que round(avg(puntuacion)::numeric, 2) en PostgreSQL. */
	private static double redondear(double promedio) {
		return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

}
