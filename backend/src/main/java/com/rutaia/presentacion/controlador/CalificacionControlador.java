package com.rutaia.presentacion.controlador;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rutaia.aplicacion.dto.calificacion.CalificacionRequest;
import com.rutaia.aplicacion.dto.calificacion.CalificacionResponse;
import com.rutaia.aplicacion.servicio.CalificacionServicio;
import com.rutaia.configuracion.OpenApiConfiguracion.Etiquetas;
import com.rutaia.presentacion.documentacion.EjemplosApi;
import com.rutaia.presentacion.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recomendaciones")
@Tag(name = Etiquetas.CALIFICACIONES)
public class CalificacionControlador {

	private final CalificacionServicio calificacionServicio;

	public CalificacionControlador(CalificacionServicio calificacionServicio) {
		this.calificacionServicio = calificacionServicio;
	}

	@PostMapping("/{id}/calificacion")
	@Operation(operationId = "calificarRecomendacion", summary = "Calificar una recomendación (E-13)",
			description = "Registra una puntuación entera de 1 a 5 y un comentario opcional de hasta 500 caracteres. "
					+ "Cada recomendación admite una sola calificación: la segunda responde 409, también si dos "
					+ "llegan a la vez (restricción única uq_calificacion_recomendacion). El id es el recomendacionId "
					+ "que devuelven E-11 y E-12.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
					content = @Content(schema = @Schema(implementation = CalificacionRequest.class),
							examples = {
								@ExampleObject(name = "Con comentario", value = EjemplosApi.CALIFICACION_REQUEST),
								@ExampleObject(name = "Sin comentario", value = EjemplosApi.CALIFICACION_REQUEST_SIN_COMENTARIO) })))
	@ApiResponse(responseCode = "201", description = "Calificación registrada",
			headers = @Header(name = "Location", description = "URL de la calificación de la recomendación",
					schema = @Schema(type = "string",
							example = "http://localhost:8080/api/recomendaciones/1/calificacion")),
			content = @Content(schema = @Schema(implementation = CalificacionResponse.class),
					examples = @ExampleObject(value = EjemplosApi.CALIFICACION)))
	@ApiResponse(responseCode = "400", description = "Puntuación ausente, fuera de 1 a 5 o decimal, comentario de "
			+ "más de 500 caracteres, id no numérico o JSON mal formado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = {
						@ExampleObject(name = "Fuera de rango", value = EjemplosApi.ERROR_PUNTUACION_FUERA_DE_RANGO),
						@ExampleObject(name = "Decimal", value = EjemplosApi.ERROR_PUNTUACION_DECIMAL) }))
	@ApiResponse(responseCode = "404", description = "La recomendación no existe",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_RECOMENDACION_NO_EXISTE)))
	@ApiResponse(responseCode = "409", description = "La recomendación ya fue calificada",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_YA_CALIFICADA)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public ResponseEntity<CalificacionResponse> calificar(
			@Parameter(description = "Id de la recomendación", example = "1") @PathVariable Long id,
			@Valid @RequestBody CalificacionRequest solicitud) {
		CalificacionResponse creada = calificacionServicio.calificar(id, solicitud);
		// La calificación es un subrecurso único de la recomendación: su URL es la misma de la petición.
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
		return ResponseEntity.created(ubicacion).body(creada);
	}

}
