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

	static final String HISTORIAL = """
			[{"consultaId":4,"fecha":"2026-11-02T22:34:47","pregunta":"Necesito desplegar aplicaciones usando contenedores.","estado":"Respondida","resumen":"Hola, soy tu orientador académico de RutaIA. Para tu objetivo de desplegar aplicaciones usando contenedores y tu interés en DevOps, te recomiendo seguir este or…","cursosRecomendados":[{"cursoId":24,"nombre":"Contenedores con Docker para desplegar aplicaciones"},{"cursoId":26,"nombre":"Integración y entrega continua con contenedores"},{"cursoId":25,"nombre":"Orquestación de contenedores con Kubernetes"}]},{"consultaId":1,"fecha":"2026-11-02T22:33:40","pregunta":"Necesito desplegar aplicaciones usando contenedores.","estado":"Error","resumen":"No fue posible generar la recomendación en este momento. Intenta de nuevo más tarde.","cursosRecomendados":[]}]""";

	static final String DETALLE_CALIFICADA = """
			{"consultaId":2,"estudianteId":1,"pregunta":"Quiero aprender a crear páginas web.","fecha":"2026-11-02T22:34:20","estado":"Respondida","respuesta":"Hola, soy tu orientador de RutaIA. Para tu interés en desarrollo web y dado que eres principiante, te recomiendo seguir este orden de aprendizaje:\\n\\nPrimero, debes tomar Fundamentos de HTML y CSS para páginas web…","mensaje":null,"recomendacionId":1,"fuentes":[{"cursoId":1,"nombre":"Fundamentos de HTML y CSS para páginas web","descripcion":"Curso para quienes quieren aprender a crear páginas web desde cero sin experiencia previa…","categoria":"Desarrollo web","nivel":"Básico","duracionHoras":40,"similitud":0.593},{"cursoId":2,"nombre":"JavaScript y manipulación del DOM","descripcion":"Aprende a programar en JavaScript para dar interactividad…","categoria":"Desarrollo web","nivel":"Intermedio","duracionHoras":50,"similitud":0.3843}],"calificacion":{"puntuacion":5,"comentario":"La ruta me sirvió para saber por dónde empezar.","fecha":"2026-11-02T22:59:15"}}""";

	static final String DETALLE_SIN_CALIFICAR = """
			{"consultaId":4,"estudianteId":7,"pregunta":"Necesito desplegar aplicaciones usando contenedores.","fecha":"2026-11-02T22:34:47","estado":"Respondida","respuesta":"Hola, soy tu orientador académico de RutaIA. Para tu objetivo de desplegar aplicaciones usando contenedores y tu interés en DevOps, te recomiendo seguir este orden de aprendizaje…","mensaje":null,"recomendacionId":3,"fuentes":[{"cursoId":24,"nombre":"Contenedores con Docker para desplegar aplicaciones","descripcion":"Aprende a empaquetar y desplegar aplicaciones usando contenedores con Docker…","categoria":"Herramientas tecnológicas","nivel":"Intermedio","duracionHoras":30,"similitud":0.6088},{"cursoId":26,"nombre":"Integración y entrega continua con contenedores","descripcion":"Aprende a automatizar la construcción, las pruebas y el despliegue de aplicaciones…","categoria":"Herramientas tecnológicas","nivel":"Intermedio","duracionHoras":35,"similitud":0.484}],"calificacion":null}""";

	static final String DETALLE_SIN_RESULTADOS = """
			{"consultaId":5,"estudianteId":8,"pregunta":"Quiero aprender cocina italiana.","fecha":"2026-11-02T22:35:03","estado":"Sin resultados","respuesta":null,"mensaje":"No encontramos cursos del catálogo relacionados con tu necesidad. Intenta describirla con otras palabras.","recomendacionId":null,"fuentes":[],"calificacion":null}""";

	static final String DETALLE_ERROR = """
			{"consultaId":1,"estudianteId":7,"pregunta":"Necesito desplegar aplicaciones usando contenedores.","fecha":"2026-11-02T22:33:40","estado":"Error","respuesta":null,"mensaje":"No fue posible generar la recomendación en este momento. Intenta de nuevo más tarde.","recomendacionId":null,"fuentes":[],"calificacion":null}""";

	static final String ERROR_ID_HISTORIAL_INVALIDO = """
			{"status":400,"error":"Bad Request","mensaje":"La solicitud contiene parámetros inválidos","ruta":"/api/estudiantes/abc/historial","fecha":"2026-11-02T23:00:00","detalles":[{"campo":"id","mensaje":"debe ser un número entero"}]}""";

	static final String ERROR_HISTORIAL_ESTUDIANTE_NO_EXISTE = """
			{"status":404,"error":"Not Found","mensaje":"No existe un estudiante con id 999999","ruta":"/api/estudiantes/999999/historial","fecha":"2026-11-02T23:00:00","detalles":[]}""";

	static final String ERROR_ID_CONSULTA_INVALIDO = """
			{"status":400,"error":"Bad Request","mensaje":"La solicitud contiene parámetros inválidos","ruta":"/api/consultas/abc","fecha":"2026-11-02T23:00:00","detalles":[{"campo":"id","mensaje":"debe ser un número entero"}]}""";

	static final String ERROR_CONSULTA_NO_EXISTE = """
			{"status":404,"error":"Not Found","mensaje":"No existe una consulta con id 999999","ruta":"/api/consultas/999999","fecha":"2026-11-02T23:00:00","detalles":[]}""";

	static final String CALIFICACION_REQUEST = """
			{"puntuacion":5,"comentario":"La ruta me sirvió para saber por dónde empezar."}""";

	static final String CALIFICACION_REQUEST_SIN_COMENTARIO = """
			{"puntuacion":4}""";

	static final String CALIFICACION = """
			{"recomendacionId":1,"puntuacion":5,"comentario":"La ruta me sirvió para saber por dónde empezar.","fecha":"2026-11-02T22:59:15"}""";

	static final String ERROR_PUNTUACION_FUERA_DE_RANGO = """
			{"status":400,"error":"Bad Request","mensaje":"La solicitud contiene datos inválidos","ruta":"/api/recomendaciones/1/calificacion","fecha":"2026-11-02T23:00:00","detalles":[{"campo":"puntuacion","mensaje":"debe estar entre 1 y 5"}]}""";

	static final String ERROR_PUNTUACION_DECIMAL = """
			{"status":400,"error":"Bad Request","mensaje":"La solicitud contiene datos inválidos","ruta":"/api/recomendaciones/1/calificacion","fecha":"2026-11-02T23:00:00","detalles":[{"campo":"puntuacion","mensaje":"tiene un tipo o formato inválido"}]}""";

	static final String ERROR_RECOMENDACION_NO_EXISTE = """
			{"status":404,"error":"Not Found","mensaje":"No existe una recomendación con id 999999","ruta":"/api/recomendaciones/999999/calificacion","fecha":"2026-11-02T23:00:00","detalles":[]}""";

	static final String ERROR_YA_CALIFICADA = """
			{"status":409,"error":"Conflict","mensaje":"La recomendación 1 ya fue calificada","ruta":"/api/recomendaciones/1/calificacion","fecha":"2026-11-02T23:00:00","detalles":[]}""";

	static final String ESTADISTICAS = """
			{"totalConsultas":5,"consultasRespondidas":3,"consultasSinResultados":1,"promedioCalificaciones":4.33,"cursoMasRecomendado":{"cursoId":1,"nombre":"Fundamentos de HTML y CSS para páginas web","vecesRecomendado":1}}""";

	static final String ESTADISTICAS_VACIAS = """
			{"totalConsultas":0,"consultasRespondidas":0,"consultasSinResultados":0,"promedioCalificaciones":null,"cursoMasRecomendado":null}""";

	static final String ERROR_INTERNO = """
			{"status":500,"error":"Internal Server Error","mensaje":"Ocurrió un error inesperado. Intenta de nuevo más tarde.","ruta":"/api/cursos","fecha":"2026-11-01T10:32:00","detalles":[]}""";

	private EjemplosApi() {
	}

}
