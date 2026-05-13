package com.proyecto.GestionVeterinaria.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Errores de validación
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(fe.getField(), fe.getDefaultMessage());
    }
    return buildError(HttpStatus.BAD_REQUEST, "Datos inválidos", fieldErrors);
  }

  // Body faltante o JSON malformado
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleMissingBody(HttpMessageNotReadableException ex) {
    return buildError(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud es requerido o tiene formato incorrecto",
        null);
  }

  // Parámetro de query faltante
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
    return buildError(HttpStatus.BAD_REQUEST, "Parámetro requerido faltante: " + ex.getParameterName(), null);
  }

  // Tipo incorrecto en path variable o query param
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return buildError(HttpStatus.BAD_REQUEST,
        "Valor inválido para el parámetro '" + ex.getName() + "': " + ex.getValue(), null);
  }

  // ResponseStatusException lanzada desde servicios
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
    return buildError(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), null);
  }

  // Ruta no encontrada
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NoResourceFoundException ex) {
    return buildError(HttpStatus.NOT_FOUND, "Recurso no encontrado: " + ex.getResourcePath(), null);
  }

  // Cualquier otro error no previsto
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
  }

  private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message, Object details) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    if (details != null) {
      body.put("details", details);
    }
    return ResponseEntity.status(status).body(body);
  }
}
