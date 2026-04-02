package com.lifeos.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Captura excepciones no controladas en cualquier controlador y devuelve
 * un JSON limpio y estructurado en lugar del stack-trace de Spring por defecto.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura RuntimeException (entidad no encontrada, acceso denegado, etc.)
     * y devuelve un JSON { "error": "mensaje" } con HTTP 400.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Captura errores de validación de @Valid (@NotBlank, @NotNull, etc.)
     * y concatena todos los mensajes de campo en un único string legible.
     * Devuelve HTTP 422 Unprocessable Entity.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        Map<String, String> body = new HashMap<>();
        body.put("error", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    /**
     * Captura cualquier excepción inesperada no contemplada arriba.
     * Devuelve HTTP 500 con un mensaje genérico (no expone stack-trace al cliente).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Error interno del servidor. Inténtalo de nuevo más tarde.");
        // El stack-trace sigue yendo a los logs del servidor, NO al cliente
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

