package com.rutaia.dominio.convertidor;

import com.rutaia.dominio.NivelExperiencia;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NivelExperienciaConverter implements AttributeConverter<NivelExperiencia, String> {

	@Override
	public String convertToDatabaseColumn(NivelExperiencia nivel) {
		return nivel == null ? null : nivel.getTexto();
	}

	@Override
	public NivelExperiencia convertToEntityAttribute(String texto) {
		return texto == null ? null : NivelExperiencia.desdeTexto(texto);
	}

}
