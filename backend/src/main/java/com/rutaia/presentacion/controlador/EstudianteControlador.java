package com.rutaia.presentacion.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rutaia.aplicacion.dto.estudiante.EstudianteRequest;
import com.rutaia.aplicacion.dto.estudiante.EstudianteResponse;
import com.rutaia.aplicacion.servicio.impl.EstudianteServicioImpl;
import com.rutaia.configuracion.OpenApiConfiguracion.Etiquetas;
import com.rutaia.presentacion.documentacion.EjemplosApi;
import com.rutaia.presentacion.error.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estudiantes")
@Tag(name = Etiquetas.ESTUDIANTES)
public class EstudianteControlador {

	private final EstudianteServicioImpl estudianteServicio;

	public EstudianteControlador(EstudianteServicioImpl estudianteServicio) {
		this.estudianteServicio = estudianteServicio;
	}

	@PostMapping
	@Operation(operationId = "registrarEstudiante", summary = "Registrar un estudiante (E-01)",
			description = "Registra nombre completo, correo, nivel de experiencia y área de interés. El correo se "
					+ "guarda sin espacios al inicio o al final y en minúsculas; no puede repetirse, sin distinguir "
					+ "mayúsculas. nivelExperiencia acepta solo Principiante, Intermedio o Avanzado.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
					content = @Content(schema = @Schema(implementation = EstudianteRequest.class),
							examples = @ExampleObject(name = "Alta válida", value = EjemplosApi.ESTUDIANTE_REQUEST))))
	@ApiResponse(responseCode = "201", description = "Estudiante registrado",
			headers = @Header(name = "Location", description = "URL del estudiante creado",
					schema = @Schema(type = "string", example = "http://localhost:8080/api/estudiantes/11")),
			content = @Content(schema = @Schema(implementation = EstudianteResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ESTUDIANTE)))
	@ApiResponse(responseCode = "400", description = "Datos inválidos, JSON mal formado o nivel fuera de C5",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_VALIDACION_ESTUDIANTE)))
	@ApiResponse(responseCode = "409", description = "El correo ya está registrado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_CORREO_DUPLICADO)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public ResponseEntity<EstudianteResponse> registrar(@Valid @RequestBody EstudianteRequest solicitud) {
		EstudianteResponse creado = estudianteServicio.registrar(solicitud);
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(creado.id())
				.toUri();
		return ResponseEntity.created(ubicacion).body(creado);
	}

	@GetMapping
	@Operation(operationId = "listarEstudiantes", summary = "Listar estudiantes (E-02)",
			description = "Devuelve todos los estudiantes registrados, ordenados por id ascendente.")
	@ApiResponse(responseCode = "200", description = "Lista de estudiantes",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = EstudianteResponse.class)),
					examples = @ExampleObject(value = "[" + EjemplosApi.ESTUDIANTE + "]")))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public List<EstudianteResponse> listar() {
		return estudianteServicio.listar();
	}

	@GetMapping("/{id}")
	@Operation(operationId = "consultarEstudiante", summary = "Consultar un estudiante por id (E-03)",
			description = "Devuelve el estudiante con el identificador indicado.")
	@ApiResponse(responseCode = "200", description = "Estudiante encontrado",
			content = @Content(schema = @Schema(implementation = EstudianteResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ESTUDIANTE)))
	@ApiResponse(responseCode = "400", description = "El id no es un número entero",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "El estudiante no existe",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_ESTUDIANTE_NO_EXISTE)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public EstudianteResponse consultar(@Parameter(description = "Id del estudiante", example = "1")
			@PathVariable Long id) {
		return estudianteServicio.consultar(id);
	}

}
