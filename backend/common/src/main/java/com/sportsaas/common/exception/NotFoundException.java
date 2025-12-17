package com.sportsaas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource is not found.
 */
public class NotFoundException extends BusinessException {

    public NotFoundException(String message) {
        super("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String entityName, Object id) {
        super("NOT_FOUND", String.format("%s not found with id: %s", entityName, id), HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String entityName, String field, Object value) {
        super("NOT_FOUND", String.format("%s not found with %s: %s", entityName, field, value), HttpStatus.NOT_FOUND);
    }
}
