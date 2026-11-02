package com.rutaia.controlador;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.rutaia.configuracion.OpenApiConfiguracion.Etiquetas;
import com.rutaia.dto.CursoAdminResponse;
import com.rutaia.dto.CursoRequest;
import com.rutaia.dto.ErrorResponse;
import com.rutaia.servicio.CursoServicio;

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
@RequestMapping("/api/admin/cursos")
@Tag(name = Etiquetas.ADMINISTRACION)
public class AdminCursoControlador {

	private final CursoServicio cursoServicio;

	public AdminCursoControlador(CursoServicio cursoServicio) {
		this.cursoServicio = cursoServicio;
	}

	@PostMapping
	@Operation(operationId = "registrarCurso", summary = "Registrar un curso (E-06)",
			description = "Registra un curso activo. Todos los campos son obligatorios; nombre y descripción no "
					+ "pueden estar vacíos, nivel acepta Básico, Intermedio o Avanzado y duracionHoras debe ser "
					+ "un entero mayor que cero.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
					content = @Content(schema = @Schema(implementation = CursoRequest.class),
							examples = @ExampleObject(name = "Curso válido", value = EjemplosApi.CURSO_REQUEST))))
	@ApiResponse(responseCode = "201", description = "Curso registrado con activo = true",
			headers = @Header(name = "Location", description = "URL del curso creado",
					schema = @Schema(type = "string", example = "http://localhost:8080/api/admin/cursos/31")),
			content = @Content(schema = @Schema(implementation = CursoAdminResponse.class),
					examples = @ExampleObject(value = EjemplosApi.CURSO_ADMIN)))
	@ApiResponse(responseCode = "400", description = "Datos inválidos, JSON mal formado o nivel fuera de C5",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_VALIDACION_CURSO)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public ResponseEntity<CursoAdminResponse> registrar(@Valid @RequestBody CursoRequest solicitud) {
		CursoAdminResponse creado = cursoServicio.registrar(solicitud);
		URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(creado.id())
				.toUri();
		return ResponseEntity.created(ubicacion).body(creado);
	}

	@GetMapping
	@Operation(operationId = "listarCursosAdmin", summary = "Listar todos los cursos (E-07)",
			description = "Devuelve los cursos activos e inactivos, ordenados por id ascendente.")
	@ApiResponse(responseCode = "200", description = "Lista de cursos con su estado",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = CursoAdminResponse.class)),
					examples = @ExampleObject(value = "[" + EjemplosApi.CURSO_ADMIN + "]")))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public List<CursoAdminResponse> listar() {
		return cursoServicio.listarTodos();
	}

	@GetMapping("/{id}")
	@Operation(operationId = "consultarCursoAdmin", summary = "Consultar un curso por id (E-08)",
			description = "Devuelve el curso, activo o inactivo, con el identificador indicado.")
	@ApiResponse(responseCode = "200", description = "Curso encontrado",
			content = @Content(schema = @Schema(implementation = CursoAdminResponse.class),
					examples = @ExampleObject(value = EjemplosApi.CURSO_ADMIN)))
	@ApiResponse(responseCode = "400", description = "El id no es un número entero",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "El curso no existe",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_CURSO_NO_EXISTE)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public CursoAdminResponse consultar(@Parameter(description = "Id del curso", example = "1")
			@PathVariable Long id) {
		return cursoServicio.consultar(id);
	}

	@PutMapping("/{id}")
	@Operation(operationId = "actualizarCurso", summary = "Actualizar un curso (E-09)",
			description = "Reemplaza nombre, descripción, categoría, nivel y duración con las mismas reglas que el "
					+ "registro. El estado activo no cambia.",
			requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
					content = @Content(schema = @Schema(implementation = CursoRequest.class),
							examples = @ExampleObject(name = "Curso válido", value = EjemplosApi.CURSO_REQUEST))))
	@ApiResponse(responseCode = "200", description = "Curso actualizado",
			content = @Content(schema = @Schema(implementation = CursoAdminResponse.class),
					examples = @ExampleObject(value = EjemplosApi.CURSO_ADMIN)))
	@ApiResponse(responseCode = "400", description = "Datos inválidos, JSON mal formado, nivel fuera de C5 o id no numérico",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_VALIDACION_CURSO)))
	@ApiResponse(responseCode = "404", description = "El curso no existe",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_CURSO_NO_EXISTE)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public CursoAdminResponse actualizar(@Parameter(description = "Id del curso", example = "1")
			@PathVariable Long id, @Valid @RequestBody CursoRequest solicitud) {
		return cursoServicio.actualizar(id, solicitud);
	}

	@PatchMapping("/{id}/desactivar")
	@Operation(operationId = "desactivarCurso", summary = "Desactivar un curso (E-10)",
			description = "Marca el curso como inactivo: deja de aparecer en el catálogo, pero sigue en la "
					+ "administración. Es idempotente: desactivar un curso ya inactivo devuelve 200.")
	@ApiResponse(responseCode = "200", description = "Curso con activo = false",
			content = @Content(schema = @Schema(implementation = CursoAdminResponse.class),
					examples = @ExampleObject(value = EjemplosApi.CURSO_DESACTIVADO)))
	@ApiResponse(responseCode = "400", description = "El id no es un número entero",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "El curso no existe",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_CURSO_NO_EXISTE)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public CursoAdminResponse desactivar(@Parameter(description = "Id del curso", example = "1")
			@PathVariable Long id) {
		return cursoServicio.desactivar(id);
	}

}
