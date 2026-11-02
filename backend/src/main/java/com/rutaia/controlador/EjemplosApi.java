package com.rutaia.controlador;

/** Ejemplos JSON que muestra Swagger en las respuestas de error y en los cuerpos de solicitud. */
final class EjemplosApi {

	static final String ESTUDIANTE_REQUEST = """
			{"nombreCompleto":"Ana María Torres","correo":"Ana.Torres@RutaIA.test","nivelExperiencia":"Principiante","areaInteres":"Desarrollo web"}""";

	static final String ESTUDIANTE = """
			{"id":11,"nombreCompleto":"Ana María Torres","correo":"ana.torres@rutaia.test","nivelExperiencia":"Principiante","areaInteres":"Desarrollo web","fechaRegistro":"2026-11-02T17:05:12"}""";

	static final String CURSO_REQUEST = """
			{"nombre":"Introducción a TypeScript","descripcion":"Curso práctico sobre tipos, interfaces y genéricos en TypeScript para crear aplicaciones web mantenibles.","categoria":"Desarrollo web","nivel":"Intermedio","duracionHoras":24}""";

	static final String CURSO_ADMIN = """
			{"id":31,"nombre":"Introducción a TypeScript","descripcion":"Curso práctico sobre tipos, interfaces y genéricos en TypeScript para crear aplicaciones web mantenibles.","categoria":"Desarrollo web","nivel":"Intermedio","duracionHoras":24,"activo":true}""";

	static final String CURSO_DESACTIVADO = """
			{"id":31,"nombre":"Introducción a TypeScript","descripcion":"Curso práctico sobre tipos, interfaces y genéricos en TypeScript para crear aplicaciones web mantenibles.","categoria":"Desarrollo web","nivel":"Intermedio","duracionHoras":24,"activo":false}""";

	static final String ERROR_VALIDACION_ESTUDIANTE = """
			{"status":400,"error":"Bad Request","mensaje":"La solicitud contiene datos inválidos","ruta":"/api/estudiantes","fecha":"2026-11-01T10:32:00","detalles":[{"campo":"correo","mensaje":"debe ser un correo válido"},{"campo":"nivelExperiencia","mensaje":"es obligatorio"}]}""";

	static final String ERROR_CORREO_DUPLICADO = """
			{"status":409,"error":"Conflict","mensaje":"El correo valeria.gomez@rutaia.test ya está registrado","ruta":"/api/estudiantes","fecha":"2026-11-01T10:32:00","detalles":[{"campo":"correo","mensaje":"ya está registrado"}]}""";

	static final String ERROR_ESTUDIANTE_NO_EXISTE = """
			{"status":404,"error":"Not Found","mensaje":"No existe un estudiante con id 999999","ruta":"/api/estudiantes/999999","fecha":"2026-11-01T10:32:00","detalles":[]}""";

	static final String ERROR_NIVEL_INVALIDO = """
			{"status":400,"error":"Bad Request","mensaje":"La solicitud contiene parámetros inválidos","ruta":"/api/cursos","fecha":"2026-11-01T10:32:00","detalles":[{"campo":"nivel","mensaje":"debe ser Básico, Intermedio o Avanzado"}]}""";

	static final String ERROR_VALIDACION_CURSO = """
			{"status":400,"error":"Bad Request","mensaje":"La solicitud contiene datos inválidos","ruta":"/api/admin/cursos","fecha":"2026-11-01T10:32:00","detalles":[{"campo":"duracionHoras","mensaje":"debe ser mayor que cero"},{"campo":"nombre","mensaje":"es obligatorio"}]}""";

	static final String ERROR_CURSO_NO_EXISTE = """
			{"status":404,"error":"Not Found","mensaje":"No existe un curso con id 999999","ruta":"/api/admin/cursos/999999","fecha":"2026-11-01T10:32:00","detalles":[]}""";

	static final String ERROR_INTERNO = """
			{"status":500,"error":"Internal Server Error","mensaje":"Ocurrió un error inesperado. Intenta de nuevo más tarde.","ruta":"/api/cursos","fecha":"2026-11-01T10:32:00","detalles":[]}""";

	private EjemplosApi() {
	}

}
