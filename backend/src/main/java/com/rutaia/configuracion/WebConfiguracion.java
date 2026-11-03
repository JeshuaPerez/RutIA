package com.rutaia.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.rutaia.dominio.NivelCurso;
import com.rutaia.dominio.NivelExperiencia;

@Configuration
public class WebConfiguracion implements WebMvcConfigurer {

	private final String[] origenesCors;

	public WebConfiguracion(@Value("${rutaia.cors.origins}") String[] origenesCors) {
		this.origenesCors = origenesCors;
	}

	/** CORS solo para /api/** con los orígenes de RUTAIA_CORS_ORIGINS (C6). */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins(origenesCors)
				.allowedMethods("GET", "POST", "PUT", "PATCH", "OPTIONS")
				.allowedHeaders("Content-Type");
	}

	/** Los parámetros de consulta usan el texto de C5 («Básico»), no el nombre de la constante. */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(String.class, NivelCurso.class,
				(Converter<String, NivelCurso>) texto -> texto.isBlank() ? null : NivelCurso.desdeTexto(texto));
		registry.addConverter(String.class, NivelExperiencia.class,
				(Converter<String, NivelExperiencia>) texto -> texto.isBlank() ? null : NivelExperiencia.desdeTexto(texto));
	}

}
