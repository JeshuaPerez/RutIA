package com.rutaia.presentacion.error;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rutaia.aplicacion.mapper.Formatos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Error", description = "Cuerpo de toda respuesta 4xx y 5xx (C6)")
public record ErrorResponse(

		@Schema(example = "400")
		int status,

		@Schema(example = "Bad Request")
		String error,

		@Schema(example = "La solicitud contiene datos inválidos")
		String mensaje,

		@Schema(example = "/api/estudiantes")
		String ruta,

		@Schema(type = "string", example = "2026-11-01T10:32:00")
		@JsonFormat(pattern = Formatos.FECHA_HORA)
		LocalDateTime fecha,

		@Schema(description = "Errores por campo; vacío si el error no es de campos")
		List<Detalle> detalles) {

	@Schema(name = "DetalleError")
	public record Detalle(

			@Schema(example = "correo")
			String campo,

			@Schema(example = "debe ser un correo válido")
			String mensaje) {
	}

}
