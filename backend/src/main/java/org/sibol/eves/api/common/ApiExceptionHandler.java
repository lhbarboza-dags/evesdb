package org.sibol.eves.api.common;

import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	public record ErrorBody(int status, String message, Map<String, String> fields) {}

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorBody> api(ApiException e) {
		return ResponseEntity.status(e.getStatus()).body(new ErrorBody(e.getStatus().value(), e.getMessage(), null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorBody> invalid(MethodArgumentNotValidException e) {
		Map<String, String> fields = e.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(f -> f.getField(), f -> String.valueOf(f.getDefaultMessage()), (a, b) -> a));
		return ResponseEntity.badRequest().body(new ErrorBody(400, "Some values are incorrect.", fields));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorBody> denied(AccessDeniedException e) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorBody(403, "You are not allowed to do this.", null));
	}

	@ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorBody> method(org.springframework.web.HttpRequestMethodNotSupportedException e) {
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new ErrorBody(405, "Method not allowed.", null));
	}

	@ExceptionHandler({ org.springframework.http.converter.HttpMessageNotReadableException.class,
			org.springframework.web.bind.MissingServletRequestParameterException.class,
			org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class })
	public ResponseEntity<ErrorBody> badInput(Exception e) {
		return ResponseEntity.badRequest().body(new ErrorBody(400, "The request is not valid.", null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorBody> other(Exception e) {
		log.error("Unhandled error", e);
		return ResponseEntity.status(500).body(new ErrorBody(500, "Unexpected server error.", null));
	}
}
