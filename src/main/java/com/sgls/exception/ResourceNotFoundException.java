package com.sgls.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * RESOURCE NOT FOUND EXCEPTION
 * -----------------------------
 * Thrown when a requested entity doesn't exist in the database.
 * e.g., GET /warehouses/999 where ID 999 doesn't exist.
 *
 * @ResponseStatus maps this exception to HTTP 404 automatically
 * when thrown from a controller (if no @ControllerAdvice handles it).
 * Our GlobalExceptionHandler also catches it for consistent formatting.
 *
 * Extends RuntimeException (unchecked) — we don't want callers to be
 * forced to try/catch everywhere. The @ControllerAdvice catches it centrally.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        // Super message: "Warehouse not found with id : 99"
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() { return resourceName; }
    public String getFieldName() { return fieldName; }
    public Object getFieldValue() { return fieldValue; }
}
