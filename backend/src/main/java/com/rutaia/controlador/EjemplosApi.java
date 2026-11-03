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

	static final String CONSULTA_REQUEST = """
			{"estudianteId":1,"pregunta":"Quiero aprender a crear páginas web."}""";

	static final String CONSULTA_RESPONDIDA = """
			{"consultaId":2,"estudianteId":1,"pregunta":"Quiero aprender a crear páginas web.","fecha":"2026-11-02T22:34:20","estado":"Respondida","respuesta":"Hola, soy tu orientador de RutaIA. Para tu interés en desarrollo web y dado que eres principiante, te recomiendo seguir este orden de aprendizaje:\\n\\nPrimero, debes tomar Fundamentos de HTML y CSS para páginas web…","mensaje":null,"recomendacionId":1,"fuentes":[{"cursoId":1,"nombre":"Fundamentos de HTML y CSS para páginas web","descripcion":"Curso para quienes quieren aprender a crear páginas web desde cero sin experiencia previa…","categoria":"Desarrollo web","nivel":"Básico","duracionHoras":40,"similitud":0.593},{"cursoId":2,"nombre":"JavaScript y manipulación del DOM","descripcion":"Aprende a programar en JavaScript para dar interactividad…","categoria":"Desarrollo web","nivel":"Intermedio","duracionHoras":50,"similitud":0.3843}]}""";

	static final String CONSULTA_SIN_RESULTADOS = """
			{"consultaId":18,"estudianteId":8,"pregunta":"Quiero aprender cocina italiana.","fecha":"2026-11-01T10:40:00","estado":"Sin resultados","respuesta":null,"mensaje":"No encontramos cursos del catálogo relacionados con tu necesidad. Intenta describirla con otras palabras.","recomendacionId":null,"fuentes":[]}""";

	static final String CONSULTA_ERROR = """
			{"consultaId":19,"estudianteId":7,"pregunta":"Necesito desplegar aplicaciones usando contenedores.","fecha":"2026-11-01T10:45:00","estado":"Error","respuesta":null,"mensaje":"No fue posible generar la recomendación en este momento. Intenta de nuevo más tarde.","recomendacionId":null,"fuentes":[]}""";

	static final String ERROR_VALIDACION_CONSULTA = """
			{"status":400,"error":"Bad Request","mensaje":"La solicitud contiene datos inválidos","ruta":"/api/consultas","fecha":"2026-11-01T10:32:00","detalles":[{"campo":"pregunta","mensaje":"es obligatoria"}]}""";

	static final String ERROR_CONSULTA_ESTUDIANTE_NO_EXISTE = """
			{"status":404,"error":"Not Found","mensaje":"No existe un estudiante con id 999999","ruta":"/api/consultas","fecha":"2026-11-01T10:32:00","detalles":[]}""";

	static final String ERROR_INTERNO = """
			{"status":500,"error":"Internal Server Error","mensaje":"Ocurrió un error inesperado. Intenta de nuevo más tarde.","ruta":"/api/cursos","fecha":"2026-11-01T10:32:00","detalles":[]}""";

	private EjemplosApi() {
	}

}
