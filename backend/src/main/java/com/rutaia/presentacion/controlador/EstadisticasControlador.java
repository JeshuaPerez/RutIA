package com.rutaia.presentacion.controlador;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rutaia.aplicacion.dto.estadistica.EstadisticasResponse;
import com.rutaia.aplicacion.servicio.impl.EstadisticasServicioImpl;
import com.rutaia.configuracion.OpenApiConfiguracion.Etiquetas;
import com.rutaia.presentacion.documentacion.EjemplosApi;
import com.rutaia.presentacion.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/estadisticas")
@Tag(name = Etiquetas.ESTADISTICAS)
public class EstadisticasControlador {

	private final EstadisticasServicioImpl estadisticasServicio;

	public EstadisticasControlador(EstadisticasServicioImpl estadisticasServicio) {
		this.estadisticasServicio = estadisticasServicio;
	}

	@GetMapping
	@Operation(operationId = "consultarEstadisticas", summary = "Consultar las estadísticas básicas (E-14)",
			description = "totalConsultas cuenta las consultas en cualquier estado; consultasRespondidas y "
					+ "consultasSinResultados, las de cada estado. promedioCalificaciones lleva 2 decimales y es null "
					+ "si no hay calificaciones. cursoMasRecomendado es el curso que más veces fue fuente de una "
					+ "recomendación; en empate gana el menor cursoId, y es null si no hay recomendaciones.")
	@ApiResponse(responseCode = "200", description = "Estadísticas calculadas",
			content = @Content(schema = @Schema(implementation = EstadisticasResponse.class),
					examples = {
						@ExampleObject(name = "Con calificaciones", value = EjemplosApi.ESTADISTICAS),
						@ExampleObject(name = "Sin datos", value = EjemplosApi.ESTADISTICAS_VACIAS) }))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public EstadisticasResponse consultar() {
		return estadisticasServicio.calcular();
	}

}
