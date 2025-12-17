package com.sportsaas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there is a conflict with existing data.
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super("CONFLICT", message, HttpStatus.CONFLICT);
    }

    public ConflictException(String entityName, String field, Object value) {
        super("CONFLICT", String.format("%s already exists with %s: %s", entityName, field, value), HttpStatus.CONFLICT);
    }
}
