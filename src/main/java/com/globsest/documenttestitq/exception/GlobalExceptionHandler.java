package com.globsest.documenttestitq.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFound(DocumentNotFoundException ex) {
        log.warn("Документ не найден: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("DOCUMENT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        log.warn("Недопустимый переход статуса: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("INVALID_STATUS_TRANSITION", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ApprovalRegistryException.class)
    public ResponseEntity<ErrorResponse> handleApprovalRegistryException(ApprovalRegistryException ex) {
        log.error("Ошибка реестра утверждений: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.of("APPROVAL_REGISTRY_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(StaleObjectStateException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(StaleObjectStateException ex) {
        log.warn("Конфликт оптимистичной блокировки: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("CONCURRENT_MODIFICATION", "Документ был изменен другим пользователем. Попробуйте еще раз.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "Ошибки валидации: " + errors.toString();
        log.warn("Ошибка валидации: {}", message);
        ErrorResponse error = ErrorResponse.of("VALIDATION_ERROR", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Ошибка ограничений: {}", ex.getMessage());
        ErrorResponse error = ErrorResponse.of("VALIDATION_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Неожиданная ошибка: {}", ex.getMessage(), ex);
        ErrorResponse error = ErrorResponse.of("INTERNAL_ERROR", "Внутренняя ошибка сервера");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
