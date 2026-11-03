package com.rutaia.aplicacion.dto.estudiante;

import java.util.Locale;

import com.rutaia.dominio.NivelExperiencia;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un estudiante")
public record EstudianteRequest(

		@Schema(example = "Ana María Torres", maxLength = 120)
		@NotBlank(message = "es obligatorio")
		@Size(max = 120, message = "debe tener como máximo 120 caracteres")
		String nombreCompleto,

		@Schema(example = "ana.torres@rutaia.test", maxLength = 254,
				description = "Se guarda sin espacios al inicio o al final y en minúsculas")
		@NotBlank(message = "es obligatorio")
		@Email(message = "debe ser un correo válido")
		@Size(max = 254, message = "debe tener como máximo 254 caracteres")
		String correo,

		@Schema(example = "Principiante", allowableValues = { "Principiante", "Intermedio", "Avanzado" })
		@NotNull(message = "es obligatorio")
		NivelExperiencia nivelExperiencia,

		@Schema(example = "Desarrollo web", maxLength = 100)
		@NotBlank(message = "es obligatorio")
		@Size(max = 100, message = "debe tener como máximo 100 caracteres")
		String areaInteres) {

	// El correo se normaliza antes de validarse, para que el formato y la longitud se comprueben sobre lo que se guarda.
	public EstudianteRequest {
		correo = correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
	}

}
