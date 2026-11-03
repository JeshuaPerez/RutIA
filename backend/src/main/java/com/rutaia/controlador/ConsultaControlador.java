package com.rutaia.controlador;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rutaia.configuracion.OpenApiConfiguracion.Etiquetas;
import com.rutaia.dto.ConsultaRequest;
import com.rutaia.dto.ConsultaResultado;
import com.rutaia.dto.ErrorResponse;
import com.rutaia.servicio.ConsultaServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consultas")
@Tag(name = Etiquetas.CONSULTAS)
public class ConsultaControlador {

	private final ConsultaServicio consultaServicio;

	public ConsultaControlador(ConsultaServicio consultaServicio) {
		this.consultaServicio = consultaServicio;
	}

	@PostMapping
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

}
