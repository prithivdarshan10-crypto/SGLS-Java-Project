package com.sgls.exception;

import com.sgls.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * GLOBAL EXCEPTION HANDLER
 * -------------------------
 * Centralizes exception handling for the ENTIRE application.
 *
 * INTERVIEW QUESTION: "How do you handle exceptions in Spring Boot?"
 *
 * WITHOUT this class:
 *   - Spring returns HTML error pages or raw stack traces
 *   - Different exceptions produce inconsistent response formats
 *   - Stack traces get exposed to clients (security risk)
 *
 * WITH @RestControllerAdvice:
 *   - ALL exceptions funnel through this class
 *   - We return consistent ApiResponse JSON for every error
 *   - We map each exception type to the correct HTTP status
 *   - We log errors centrally for monitoring
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 *   @ControllerAdvice: applies to all @Controller/@RestController classes
 *   @ResponseBody: serializes return value to JSON automatically
 *
 * @ExceptionHandler(SomeException.class) marks a method to handle
 * a specific exception type wherever it's thrown.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * handleResourceNotFoundException — 404 Not Found.
     * Triggered by: ResourceNotFoundException in any service/controller.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * handleBadRequestException — 400 Bad Request.
     * Triggered by: BadRequestException (duplicate email, invalid state, etc.)
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * handleValidationErrors — 400 Bad Request for @Valid failures.
     *
     * When a controller has @Valid on a @RequestBody parameter,
     * Spring validates all @NotBlank/@Email/@Size annotations.
     * If any fail, MethodArgumentNotValidException is thrown.
     *
     * We extract all field-level errors into a map:
     *   {"username": "Username is required", "email": "Invalid email format"}
     *
     * INTERVIEW: "How does @Valid work?"
     *   @Valid triggers JSR 380 (Bean Validation) on the argument.
     *   Spring's MethodValidationInterceptor iterates annotations,
     *   collects ConstraintViolations, and throws this exception.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation errors: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }

    /**
     * handleBadCredentials — 401 Unauthorized for wrong password.
     * Spring Security throws BadCredentialsException during login.
     * We catch it here and return a user-friendly message.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex) {
        log.warn("Failed login attempt: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
    }

    /**
     * handleAccessDenied — 403 Forbidden for RBAC violations.
     * Thrown when a user tries to access an endpoint for a role they don't have.
     * e.g., EMPLOYEE trying to call /api/admin/...
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("You don't have permission to perform this action"));
    }

    /**
     * handleGenericException — 500 Internal Server Error fallback.
     * Catches ALL other uncaught exceptions as a safety net.
     * IMPORTANT: We log the full stack trace but only return a generic
     * message to the client (no stack trace exposure to attackers).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex); // Full stack trace in logs
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again."));
    }
}
