package com.rutaia.aplicacion.puerto;

/**
 * Puerto de salida hacia el motor que genera la recomendación (RF 06 y RF 08). La aplicación solo conoce esta
 * interfaz; el adaptador que habla con el motor real vive en la capa de integración (patrón Adapter).
 *
 * <p>Contrato que toda implementación debe cumplir, para que cualquiera pueda sustituir a otra sin cambiar
 * {@code ConsultaServicioImpl}:
 * <ul>
 * <li>Devuelve un {@link ResultadoRecomendacion} Respondida, con la respuesta no vacía y de 1 a 5 fuentes sin cursoId
 * repetido y con similitud entre 0 y 1, o Sin resultados, sin respuesta ni fuentes.</li>
 * <li>Si el motor no responde, responde con error o devuelve algo que no cumple lo anterior, lanza
 * {@link FallaMotorException} con el motivo técnico. No lanza otras excepciones.</li>
 * <li>Nunca devuelve null y no reintenta.</li>
 * <li>No comprueba que los cursos de las fuentes existan o estén activos en PostgreSQL: eso lo hace la aplicación al
 * guardar.</li>
 * </ul>
 * Se llama fuera de toda transacción, porque puede tardar varios segundos.
 */
public interface MotorRecomendacion {

	/**
	 * @param solicitud la consulta ya guardada en Pendiente, con el nivel y el área del estudiante
	 * @return Respondida (1 a 5 fuentes) o Sin resultados; nunca null
	 * @throws FallaMotorException si no se obtuvo un resultado que cumpla el contrato
	 */
	ResultadoRecomendacion recomendar(SolicitudRecomendacion solicitud);

}
