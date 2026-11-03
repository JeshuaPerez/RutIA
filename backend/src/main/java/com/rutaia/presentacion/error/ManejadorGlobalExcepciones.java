package com.rutaia.presentacion.error;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.rutaia.aplicacion.excepcion.ConflictoException;
import com.rutaia.aplicacion.excepcion.RecursoNoEncontradoException;


import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.StreamReadException;

/**
 * Traduce toda excepción al cuerpo de error de C6. Hereda de ResponseEntityExceptionHandler para que las
 * excepciones propias de Spring MVC (405, 415, ruta inexistente…) conserven su código pero usen el mismo cuerpo.
 */
@RestControllerAdvice
public class ManejadorGlobalExcepciones extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalExcepciones.class);

	static final String MSG_DATOS_INVALIDOS = "La solicitud contiene datos inválidos";
	static final String MSG_JSON_MAL_FORMADO = "El cuerpo de la solicitud no es un JSON válido";
	static final String MSG_PARAMETRO_INVALIDO = "La solicitud contiene parámetros inválidos";
	static final String MSG_ERROR_INESPERADO = "Ocurrió un error inesperado. Intenta de nuevo más tarde.";

	@ExceptionHandler(RecursoNoEncontradoException.class)
	public ResponseEntity<Object> manejarNoEncontrado(RecursoNoEncontradoException ex, WebRequest request) {
		return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of(), request);
	}

	@ExceptionHandler(ConflictoException.class)
	public ResponseEntity<Object> manejarConflicto(ConflictoException ex, WebRequest request) {
		List<ErrorResponse.Detalle> detalles = ex.getCampo() == null
				? List.of()
				: List.of(new ErrorResponse.Detalle(ex.getCampo(), "ya está registrado"));
		return respuesta(HttpStatus.CONFLICT, ex.getMessage(), detalles, request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> manejarInesperado(Exception ex, WebRequest request) {
		log.error("Error inesperado en {}", ruta(request), ex);
		return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, MSG_ERROR_INESPERADO, List.of(), request);
	}

	/** Errores de Jakarta Validation en el cuerpo (@Valid). */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<ErrorResponse.Detalle> detalles = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> new ErrorResponse.Detalle(error.getField(), error.getDefaultMessage()))
				.sorted((a, b) -> a.campo().compareTo(b.campo()))
				.toList();
		return respuesta(HttpStatus.BAD_REQUEST, MSG_DATOS_INVALIDOS, detalles, request);
	}

	/** JSON mal formado, enumerado inválido o tipo de dato incorrecto en el cuerpo. */
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		JacksonException jackson = buscarCausa(ex, JacksonException.class);
		if (jackson == null || jackson instanceof StreamReadException || jackson.getPath().isEmpty()) {
			return respuesta(HttpStatus.BAD_REQUEST, MSG_JSON_MAL_FORMADO, List.of(), request);
		}
		String campo = jackson.getPath().stream()
				.map(referencia -> referencia.getPropertyName() != null
						? referencia.getPropertyName()
						: "[" + referencia.getIndex() + "]")
				.collect(Collectors.joining("."));
		String mensaje = mensajeDeEnumerado(ex);
		if (mensaje == null) {
			mensaje = "tiene un tipo o formato inválido";
		}
		return respuesta(HttpStatus.BAD_REQUEST, MSG_DATOS_INVALIDOS,
				List.of(new ErrorResponse.Detalle(campo, mensaje)), request);
	}

	/** Parámetro de ruta o de consulta que no se puede convertir (id no numérico, nivel inválido). */
	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		String campo = ex instanceof MethodArgumentTypeMismatchException mismatch ? mismatch.getName()
				: ex.getPropertyName();
		String mensaje = mensajeDeEnumerado(ex);
		if (mensaje == null) {
			boolean numerico = ex.getRequiredType() != null && Number.class.isAssignableFrom(ex.getRequiredType());
			mensaje = numerico ? "debe ser un número entero" : "tiene un valor inválido";
		}
		return respuesta(HttpStatus.BAD_REQUEST, MSG_PARAMETRO_INVALIDO,
				List.of(new ErrorResponse.Detalle(campo, mensaje)), request);
	}

	/** Resto de excepciones de Spring MVC: mismo cuerpo, código original. */
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		String mensaje = switch (statusCode.value()) {
			case 404 -> "El recurso solicitado no existe";
			case 405 -> "Método HTTP no permitido para esta ruta";
			case 415 -> "El tipo de contenido no es compatible; usa application/json";
			default -> status != null && status.is5xxServerError() ? MSG_ERROR_INESPERADO : MSG_DATOS_INVALIDOS;
		};
		if (statusCode.is5xxServerError()) {
			log.error("Error en {}", ruta(request), ex);
		}
		ErrorResponse cuerpo = cuerpo(statusCode, mensaje, List.of(), request);
		return ResponseEntity.status(statusCode).headers(headers).body(cuerpo);
	}

	private ResponseEntity<Object> respuesta(HttpStatus status, String mensaje, List<ErrorResponse.Detalle> detalles,
			WebRequest request) {
		return ResponseEntity.status(status).body(cuerpo(status, mensaje, detalles, request));
	}

	private static ErrorResponse cuerpo(HttpStatusCode statusCode, String mensaje,
			List<ErrorResponse.Detalle> detalles, WebRequest request) {
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		return new ErrorResponse(
				statusCode.value(),
				status != null ? status.getReasonPhrase() : "Error",
				mensaje,
				ruta(request),
				LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
				detalles);
	}

	private static String ruta(WebRequest request) {
		return request instanceof ServletWebRequest servlet ? servlet.getRequest().getRequestURI() : null;
	}

	/** Mensaje de los enumerados de C5 («debe ser …»); null si la causa es otra, para no exponer mensajes internos. */
	private static String mensajeDeEnumerado(Throwable ex) {
		IllegalArgumentException causa = buscarCausa(ex, IllegalArgumentException.class);
		String mensaje = causa != null ? causa.getMessage() : null;
		return mensaje != null && mensaje.startsWith("debe ser ") ? mensaje : null;
	}

	private static <T extends Throwable> T buscarCausa(Throwable ex, Class<T> tipo) {
		for (Throwable causa = ex; causa != null; causa = causa.getCause()) {
			if (tipo.isInstance(causa)) {
				return tipo.cast(causa);
			}
			if (causa.getCause() == causa) {
				break;
			}
		}
		return null;
	}

}
