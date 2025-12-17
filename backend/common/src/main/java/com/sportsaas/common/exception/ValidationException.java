package com.sportsaas.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exception thrown when validation fails.
 */
@Getter
public class ValidationException extends BusinessException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = Map.of();
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", "Validation failed", HttpStatus.BAD_REQUEST);
        this.fieldErrors = fieldErrors;
    }
}
