package com.rutaia.aplicacion.dto.calificacion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Calificación de una recomendación (RF 17)")
public record CalificacionRequest(

		@Schema(example = "5", minimum = "1", maximum = "5", description = "Entero de 1 a 5; un decimal se rechaza")
		@NotNull(message = "es obligatoria")
		@Min(value = 1, message = "debe estar entre 1 y 5")
		@Max(value = 5, message = "debe estar entre 1 y 5")
		Integer puntuacion,

		@Schema(example = "La ruta me sirvió para saber por dónde empezar.", maxLength = 500, nullable = true,
				description = "Opcional. Se guarda sin espacios al inicio o al final; vacío o solo espacios se guarda como null")
		@Size(max = 500, message = "debe tener como máximo 500 caracteres")
		String comentario) {

	// El trim va antes de validar, para que la longitud se compruebe sobre lo que se guarda.
	public CalificacionRequest {
		comentario = comentario == null || comentario.isBlank() ? null : comentario.trim();
	}

}
