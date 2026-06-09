package com.sgls.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API RESPONSE WRAPPER
 * --------------------
 * A consistent response envelope for ALL API endpoints.
 *
 * INTERVIEW QUESTION: "Why wrap all responses in a standard object?"
 *
 * Without a wrapper, different endpoints return different shapes:
 *   GET /warehouses    → [{"id":1,...}, {"id":2,...}]
 *   POST /auth/login   → {"token":"..."}
 *   DELETE /product/1  → 204 No Content
 *   Error              → {"error":"..."}  ← different format!
 *
 * This makes client-side handling messy. With ApiResponse:
 *   Every endpoint returns:
 *   {
 *     "success": true/false,
 *     "message": "human-readable message",
 *     "data": { ...the actual payload... },
 *     "timestamp": "2024-01-15T10:30:00"
 *   }
 *
 * The client ALWAYS knows what to expect.
 *
 * Generic type <T> allows: ApiResponse<JwtResponse>, ApiResponse<List<Warehouse>>, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ---- Static factory methods for common cases ----

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operation successful")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
