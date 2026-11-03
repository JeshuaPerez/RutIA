package com.rutaia.aplicacion.servicio;

import com.rutaia.aplicacion.dto.estadistica.EstadisticasResponse;

/** Estadísticas básicas de uso (RF 18; E-14). */
public interface EstadisticasServicio {

	EstadisticasResponse calcular();

}
