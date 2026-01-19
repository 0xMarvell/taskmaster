package com.marv.taskmaster.exceptions;

import com.marv.taskmaster.models.dto.response.generic.BaseResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j; // <--- 1. Use Logger
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice; // <--- 2. Cleaner annotation

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Validation Errors (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                BaseResponse.error(errorMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    // Resource Not Found (404)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> handleNotFound(EntityNotFoundException ex) {
        return new ResponseEntity<>(
                BaseResponse.error(ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    // Database Conflicts (409)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<String>> handleSqlException(DataIntegrityViolationException ex) {
        String message = ex.getMessage() != null && !ex.getMessage().contains("constraint")
                ? ex.getMessage()
                : "Database conflict: Duplicate entry or constraint violation";

        return new ResponseEntity<>(
                BaseResponse.error(message),
                HttpStatus.CONFLICT
        );
    }

    // Bad Arguments (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<BaseResponse<String>> handleArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                BaseResponse.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    // Access Denied (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<String>> handleAccessDenied(AccessDeniedException ex) {
        return new ResponseEntity<>(
                BaseResponse.error("You do not have permission to perform this action"),
                HttpStatus.FORBIDDEN
        );
    }

    // Fallback (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<String>> handleGeneral(Exception ex) {
        log.error("Unhandled exception occurred: ", ex);
        return new ResponseEntity<>(
                BaseResponse.error("An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}