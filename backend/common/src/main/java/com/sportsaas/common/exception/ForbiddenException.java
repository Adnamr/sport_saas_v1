package com.sportsaas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user doesn't have permission to access a resource.
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    public ForbiddenException() {
        super("FORBIDDEN", "You don't have permission to access this resource", HttpStatus.FORBIDDEN);
    }
}
