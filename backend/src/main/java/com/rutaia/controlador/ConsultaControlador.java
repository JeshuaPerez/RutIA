package com.rutaia.controlador;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rutaia.configuracion.OpenApiConfiguracion.Etiquetas;
import com.rutaia.dto.ConsultaDetalle;
import com.rutaia.dto.ConsultaRequest;
import com.rutaia.dto.ConsultaResultado;
import com.rutaia.dto.ErrorResponse;
import com.rutaia.servicio.ConsultaServicio;
import com.rutaia.servicio.HistorialServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

// Las etiquetas van por método: springdoc suma la de la clase a la del método, y E-12 va solo en «Historial y detalle».
@RestController
@RequestMapping("/api/consultas")
public class ConsultaControlador {

	private final ConsultaServicio consultaServicio;
	private final HistorialServicio historialServicio;

	public ConsultaControlador(ConsultaServicio consultaServicio, HistorialServicio historialServicio) {
		this.consultaServicio = consultaServicio;
		this.historialServicio = historialServicio;
	}

	@PostMapping
	@Tag(name = Etiquetas.CONSULTAS)
	@Operation(operationId = "registrarConsulta", summary = "Enviar una consulta y obtener la recomendación (E-11)",
			description = "Valida la petición, comprueba que el estudiante exista, guarda la consulta en Pendiente, "
					+ "llama al flujo «Consulta RAG» de n8n con la pregunta y el nivel y el área del estudiante, y "
					+ "guarda el estado final. Responde 201 en cualquier estado final: Respondida (respuesta y de 1 a "
					+ "5 fuentes, con los datos del curso tomados de PostgreSQL), Sin resultados o Error (con el "
					+ "mensaje fijo; el detalle técnico solo queda en la BD y los logs). Los 400 y 404 no crean la "
					+ "consulta ni llaman a n8n.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
					content = @Content(schema = @Schema(implementation = ConsultaRequest.class),
							examples = @ExampleObject(name = "Consulta válida", value = EjemplosApi.CONSULTA_REQUEST))))
	@ApiResponse(responseCode = "201", description = "Consulta registrada en su estado final",
			headers = @Header(name = "Location", description = "URL de la consulta creada",
					schema = @Schema(type = "string", example = "http://localhost:8080/api/consultas/12")),
			content = @Content(schema = @Schema(implementation = ConsultaResultado.class),
					examples = {
						@ExampleObject(name = "Respondida", value = EjemplosApi.CONSULTA_RESPONDIDA),
						@ExampleObject(name = "Sin resultados", value = EjemplosApi.CONSULTA_SIN_RESULTADOS),
						@ExampleObject(name = "Error", value = EjemplosApi.CONSULTA_ERROR) }))
	@ApiResponse(responseCode = "400", description = "Pregunta vacía o de más de 500 caracteres, estudianteId "
			+ "ausente o no positivo, o JSON mal formado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_VALIDACION_CONSULTA)))
	@ApiResponse(responseCode = "404", description = "El estudiante no existe",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_CONSULTA_ESTUDIANTE_NO_EXISTE)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public ResponseEntity<ConsultaResultado> registrar(@Valid @RequestBody ConsultaRequest solicitud) {
		ConsultaResultado resultado = consultaServicio.registrar(solicitud);
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(resultado.consultaId())
				.toUri();
		return ResponseEntity.created(ubicacion).body(resultado);
	}

	@GetMapping("/{id}")
	@Tag(name = Etiquetas.HISTORIAL)
	@Operation(operationId = "consultarDetalleConsulta", summary = "Ver el detalle completo de una consulta (E-12)",
			description = "Devuelve lo mismo que respondió E-11 para esa consulta (fecha, pregunta, estado, respuesta o "
					+ "mensaje fijo, recomendacionId y fuentes por similitud descendente, con los datos del curso de "
					+ "PostgreSQL) más `calificacion`: null si la recomendación no fue calificada o si la consulta no "
					+ "terminó Respondida.")
	@ApiResponse(responseCode = "200", description = "Detalle de la consulta",
			content = @Content(schema = @Schema(implementation = ConsultaDetalle.class),
					examples = {
						@ExampleObject(name = "Respondida y calificada", value = EjemplosApi.DETALLE_CALIFICADA),
						@ExampleObject(name = "Respondida sin calificar", value = EjemplosApi.DETALLE_SIN_CALIFICAR),
						@ExampleObject(name = "Sin resultados", value = EjemplosApi.DETALLE_SIN_RESULTADOS),
						@ExampleObject(name = "Error", value = EjemplosApi.DETALLE_ERROR) }))
	@ApiResponse(responseCode = "400", description = "El id no es un número entero",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_ID_CONSULTA_INVALIDO)))
	@ApiResponse(responseCode = "404", description = "La consulta no existe",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_CONSULTA_NO_EXISTE)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public ConsultaDetalle consultarDetalle(@Parameter(description = "Id de la consulta", example = "2")
			@PathVariable Long id) {
		return historialServicio.detalle(id);
	}

}
