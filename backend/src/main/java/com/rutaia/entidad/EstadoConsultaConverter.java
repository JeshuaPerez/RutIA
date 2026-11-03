package com.rutaia.entidad;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoConsultaConverter implements AttributeConverter<EstadoConsulta, String> {

	@Override
	public String convertToDatabaseColumn(EstadoConsulta estado) {
		return estado == null ? null : estado.getTexto();
	}

	@Override
	public EstadoConsulta convertToEntityAttribute(String texto) {
		return texto == null ? null : EstadoConsulta.desdeTexto(texto);
	}

}
