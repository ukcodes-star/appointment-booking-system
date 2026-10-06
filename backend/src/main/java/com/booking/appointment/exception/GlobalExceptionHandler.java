package com.booking.appointment.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("status", ex.getStatusCode().value());
        err.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("status", HttpStatus.BAD_REQUEST.value());
        String msg = (ex.getBindingResult().getFieldError() != null)
                ? ex.getBindingResult().getFieldError().getDefaultMessage()
                : "Validation error";
        err.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("status", HttpStatus.BAD_REQUEST.value());
        err.put("message", "Invalid input or enum value");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("status", HttpStatus.BAD_REQUEST.value());
        if ("type".equalsIgnoreCase(ex.getName())) {
            err.put("message", "Invalid service type");
        } else {
            err.put("message", "Invalid parameter: " + ex.getName());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("status", HttpStatus.BAD_REQUEST.value());
        err.put("message", "Missing required parameter: " + ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("status", HttpStatus.FORBIDDEN.value());
        err.put("message", "Access is denied");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("status", HttpStatus.CONFLICT.value());
        err.put("message", "Duplicate record or constraint violation");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("status", HttpStatus.BAD_REQUEST.value());
        err.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        Map<String, Object> err = new HashMap<>();
        err.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        err.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}