package com.rutaia.configuracion;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import com.rutaia.configuracion.VerificacionEntorno.VariableFaltanteException;

/** Registrado en META-INF/spring.factories. */
public class VariableFaltanteFailureAnalyzer extends AbstractFailureAnalyzer<VariableFaltanteException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, VariableFaltanteException cause) {
		return new FailureAnalysis(
				"Falta la variable " + cause.getVariable() + " o está vacía. Spring Boot la lee del archivo .env "
						+ "de la raíz del repositorio (spring.config.import=optional:file:../.env[.properties]) "
						+ "o de las variables de entorno del sistema.",
				"Copia .env.example como .env en la raíz del repositorio, escribe el valor de " + cause.getVariable()
						+ " (sin comillas ni espacios) y ejecuta bootRun desde la carpeta backend/.",
				cause);
	}

}
