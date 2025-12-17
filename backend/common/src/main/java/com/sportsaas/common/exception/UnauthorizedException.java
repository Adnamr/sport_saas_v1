package com.sportsaas.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails or is required.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException() {
        super("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
    }
}
