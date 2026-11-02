package com.rutaia.configuracion;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Detiene el arranque antes de intentar conectarse a PostgreSQL si falta una variable obligatoria del .env
 * (o del entorno del sistema). El mensaje lo presenta {@link VariableFaltanteFailureAnalyzer}.
 */
public class VerificacionEntorno implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	private static final String[] OBLIGATORIAS = { "RUTAIA_DB_PASSWORD" };

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		for (String variable : OBLIGATORIAS) {
			String valor = event.getEnvironment().getProperty(variable);
			if (valor == null || valor.isBlank()) {
				throw new VariableFaltanteException(variable);
			}
		}
	}

	public static class VariableFaltanteException extends IllegalStateException {

		private final String variable;

		VariableFaltanteException(String variable) {
			super("Falta la variable " + variable + " o está vacía");
			this.variable = variable;
		}

		public String getVariable() {
			return variable;
		}

	}

}
