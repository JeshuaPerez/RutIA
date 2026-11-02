package com.rutaia.entidad;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NivelCursoConverter implements AttributeConverter<NivelCurso, String> {

	@Override
	public String convertToDatabaseColumn(NivelCurso nivel) {
		return nivel == null ? null : nivel.getTexto();
	}

	@Override
	public NivelCurso convertToEntityAttribute(String texto) {
		return texto == null ? null : NivelCurso.desdeTexto(texto);
	}

}
