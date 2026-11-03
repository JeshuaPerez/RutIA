package com.rutaia.presentacion.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rutaia.aplicacion.dto.consulta.HistorialItemResponse;
import com.rutaia.aplicacion.servicio.impl.HistorialServicioImpl;
import com.rutaia.configuracion.OpenApiConfiguracion.Etiquetas;
import com.rutaia.presentacion.documentacion.EjemplosApi;
import com.rutaia.presentacion.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/estudiantes")
@Tag(name = Etiquetas.HISTORIAL)
public class HistorialControlador {

	private final HistorialServicioImpl historialServicio;

	public HistorialControlador(HistorialServicioImpl historialServicio) {
		this.historialServicio = historialServicio;
	}

	@GetMapping("/{id}/historial")
	@Operation(operationId = "consultarHistorial", summary = "Consultar el historial de un estudiante (E-04)",
			description = "Devuelve las consultas del estudiante de la más reciente a la más antigua, con fecha, "
					+ "pregunta, estado, resumen y cursos recomendados. El resumen de una Respondida son los primeros "
					+ "160 caracteres de la respuesta, más «…» si se cortó; en Sin resultados, Error y Pendiente es el "
					+ "mensaje fijo de C6. cursosRecomendados va por similitud descendente ([] si no hay). Para ver un "
					+ "registro completo, usa E-12 con su consultaId.")
	@ApiResponse(responseCode = "200", description = "Historial del estudiante; [] si no tiene consultas",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = HistorialItemResponse.class)),
					examples = {
						@ExampleObject(name = "Con consultas", value = EjemplosApi.HISTORIAL),
						@ExampleObject(name = "Sin consultas", value = "[]") }))
	@ApiResponse(responseCode = "400", description = "El id no es un número entero",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_ID_HISTORIAL_INVALIDO)))
	@ApiResponse(responseCode = "404", description = "El estudiante no existe",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_HISTORIAL_ESTUDIANTE_NO_EXISTE)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public List<HistorialItemResponse> historial(@Parameter(description = "Id del estudiante", example = "7")
			@PathVariable Long id) {
		return historialServicio.historial(id);
	}

}
