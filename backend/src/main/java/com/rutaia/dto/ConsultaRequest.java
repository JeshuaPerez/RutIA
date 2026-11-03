package com.rutaia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Necesidad de formación escrita por el estudiante")
public record ConsultaRequest(

		@Schema(example = "1", minimum = "1")
		@NotNull(message = "es obligatorio")
		@Positive(message = "debe ser mayor que cero")
		Long estudianteId,

		@Schema(example = "Quiero aprender a crear páginas web.", maxLength = 500,
				description = "Se guarda sin espacios al inicio o al final")
		@NotBlank(message = "es obligatoria")
		@Size(max = 500, message = "debe tener como máximo 500 caracteres")
		String pregunta) {

	// El trim va antes de validar, para que «no vacía» y la longitud se comprueben sobre lo que se guarda.
	public ConsultaRequest {
		pregunta = pregunta == null ? null : pregunta.trim();
	}

}
