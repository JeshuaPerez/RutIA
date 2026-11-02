package com.rutaia.controlador;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rutaia.configuracion.OpenApiConfiguracion.Etiquetas;
import com.rutaia.dto.CursoCatalogoResponse;
import com.rutaia.dto.ErrorResponse;
import com.rutaia.entidad.NivelCurso;
import com.rutaia.servicio.CursoServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cursos")
@Tag(name = Etiquetas.CATALOGO)
public class CatalogoCursoControlador {

	private static final String EJEMPLO_CATALOGO = """
			[{"id":1,"nombre":"Fundamentos de HTML y CSS para páginas web","descripcion":"Curso para quienes quieren aprender a crear páginas web desde cero...","categoria":"Desarrollo web","nivel":"Básico","duracionHoras":40}]""";

	private final CursoServicio cursoServicio;

	public CatalogoCursoControlador(CursoServicio cursoServicio) {
		this.cursoServicio = cursoServicio;
	}

	@GetMapping
	@Operation(operationId = "listarCatalogo", summary = "Listar el catálogo de cursos activos (E-05)",
			description = "Devuelve solo los cursos activos, ordenados por id ascendente, con nombre, descripción, "
					+ "categoría, nivel y duración. Los filtros son opcionales y se combinan; la categoría debe "
					+ "coincidir exactamente.")
	@ApiResponse(responseCode = "200", description = "Cursos activos que cumplen los filtros (puede ser vacía)",
			content = @Content(array = @ArraySchema(schema = @Schema(implementation = CursoCatalogoResponse.class)),
					examples = @ExampleObject(value = EJEMPLO_CATALOGO)))
	@ApiResponse(responseCode = "400", description = "Nivel fuera de C5",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_NIVEL_INVALIDO)))
	@ApiResponse(responseCode = "500", description = "Error inesperado",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class),
					examples = @ExampleObject(value = EjemplosApi.ERROR_INTERNO)))
	public List<CursoCatalogoResponse> listar(
			@Parameter(description = "Categoría exacta", example = "Desarrollo web")
			@RequestParam(required = false) String categoria,
			@Parameter(description = "Nivel del curso", schema = @Schema(type = "string",
					allowableValues = { "Básico", "Intermedio", "Avanzado" }), example = "Básico")
			@RequestParam(required = false) NivelCurso nivel) {
		return cursoServicio.listarCatalogo(categoria, nivel);
	}

}
