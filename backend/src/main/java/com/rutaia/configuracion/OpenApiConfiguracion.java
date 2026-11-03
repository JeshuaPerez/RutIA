package com.rutaia.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
public class OpenApiConfiguracion {

	@Bean
	OpenAPI rutaiaOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("RutaIA API")
						.version("0.0.1")
						.description("API REST de RutaIA, aplicación de orientación académica que recomienda cursos "
								+ "del catálogo institucional. Toda respuesta 4xx/5xx usa el cuerpo `Error`. "
								+ "Las operaciones de administración de cursos no requieren autenticación."))
				.addServersItem(new Server().url("http://localhost:8080").description("Local"))
				.addTagsItem(new Tag().name(Etiquetas.ESTUDIANTES)
						.description("Registro y consulta de estudiantes (RF 01 y RF 02)"))
				.addTagsItem(new Tag().name(Etiquetas.CATALOGO)
						.description("Catálogo público de cursos activos (RF 04)"))
				.addTagsItem(new Tag().name(Etiquetas.ADMINISTRACION)
						.description("Registro, consulta, actualización y desactivación de cursos (RF 03)"))
				.addTagsItem(new Tag().name(Etiquetas.CONSULTAS)
						.description("Consultas en lenguaje natural y recomendación de cursos (RF 06–08, RF 13 y RF 14)"))
				.addTagsItem(new Tag().name(Etiquetas.HISTORIAL)
						.description("Historial de un estudiante y detalle completo de cada consulta (RF 02 y RF 16)"))
				.addTagsItem(new Tag().name(Etiquetas.CALIFICACIONES)
						.description("Calificación de las recomendaciones (RF 17)"))
				.addTagsItem(new Tag().name(Etiquetas.ESTADISTICAS)
						.description("Estadísticas básicas de uso (RF 18)"));
	}

	public static final class Etiquetas {

		public static final String ESTUDIANTES = "Estudiantes";
		public static final String CATALOGO = "Catálogo de cursos";
		public static final String ADMINISTRACION = "Administración de cursos";
		public static final String CONSULTAS = "Consultas";
		public static final String HISTORIAL = "Historial y detalle";
		public static final String CALIFICACIONES = "Calificaciones";
		public static final String ESTADISTICAS = "Estadísticas";

		private Etiquetas() {
		}

	}

}
