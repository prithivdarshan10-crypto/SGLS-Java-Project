package com.sgls.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * BAD REQUEST EXCEPTION
 * ---------------------
 * Thrown when the request is syntactically valid but semantically wrong.
 * Examples:
 *   - Duplicate username during registration
 *   - Updating a warehouse with more stock than its capacity
 *   - Assigning an employee to a non-existent department
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
