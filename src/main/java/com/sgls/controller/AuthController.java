package com.sgls.controller;

import com.sgls.dto.request.LoginRequest;
import com.sgls.dto.request.RegisterRequest;
import com.sgls.dto.response.ApiResponse;
import com.sgls.dto.response.JwtResponse;
import com.sgls.entity.User;
import com.sgls.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AUTHENTICATION CONTROLLER
 * --------------------------
 * Handles HTTP requests for auth endpoints.
 *
 * INTERVIEW QUESTION: "What is the role of a Controller in Spring Boot?"
 * The Controller is the HTTP layer. Its ONLY jobs are:
 *   1. Receive HTTP requests (@GetMapping, @PostMapping etc.)
 *   2. Parse and validate input (@Valid, @RequestBody)
 *   3. Call the service layer
 *   4. Return HTTP responses (ResponseEntity with status + body)
 *
 * Business logic (database calls, password hashing, JWT generation)
 * belongs in the SERVICE, not here.
 *
 * @RestController = @Controller + @ResponseBody
 *   @Controller   : marks this as a Spring MVC controller
 *   @ResponseBody : serializes return values to JSON automatically
 *
 * @RequestMapping("/api/auth") : base path for all endpoints here
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * --------------------
     * Public endpoint — no authentication required (configured in WebSecurityConfig).
     *
     * @Valid — triggers Bean Validation on LoginRequest fields.
     *   If username or password is blank → 400 Bad Request before this method runs.
     *   GlobalExceptionHandler formats the error response.
     *
     * @RequestBody — tells Spring to deserialize the JSON body into LoginRequest.
     *
     * ResponseEntity<ApiResponse<JwtResponse>> — the full return type reads:
     *   ResponseEntity     = HTTP response with status code
     *   ApiResponse<...>   = our standard wrapper { success, message, data }
     *   JwtResponse        = the actual token + user info
     *
     * EXAMPLE REQUEST:
     *   POST /api/auth/login
     *   Content-Type: application/json
     *   { "username": "admin", "password": "admin123" }
     *
     * EXAMPLE RESPONSE:
     *   200 OK
     *   { "success": true, "message": "Login successful",
     *     "data": { "token": "eyJ...", "type": "Bearer",
     *               "username": "admin", "role": "ADMIN", ... } }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        log.info("Login request for user: {}", loginRequest.getUsername());

        JwtResponse jwtResponse = authService.authenticate(loginRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", jwtResponse)
        );
    }

    /**
     * POST /api/auth/register
     * -----------------------
     * ADMIN-ONLY endpoint for creating new user accounts.
     *
     * @PreAuthorize("hasRole('ADMIN')") — checked BEFORE the method runs.
     *   If the calling user doesn't have ROLE_ADMIN in SecurityContext,
     *   Spring throws AccessDeniedException → GlobalExceptionHandler → 403.
     *
     * Why not open registration?
     *   This is an internal enterprise platform. Employee accounts are
     *   created by admins, not self-registered. Keeps the user base controlled.
     *
     * Returns 201 Created (not 200 OK) — the REST standard for resource creation.
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        log.info("Registration request for username: {}", registerRequest.getUsername());

        User newUser = authService.register(registerRequest);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User registered successfully",
                        "User created with ID: " + newUser.getId()
                ));
    }

    /**
     * GET /api/auth/me
     * ----------------
     * Returns the current authenticated user's info.
     * Used by the frontend to refresh user display on page load.
     *
     * @AuthenticationPrincipal is a Spring annotation that injects
     * the current user's UserDetails from the SecurityContext.
     * It's cleaner than: SecurityContextHolder.getContext().getAuthentication()
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser(
            org.springframework.security.core.annotation.AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success("Current user: " + userDetails.getUsername() +
                        " | Role: " + userDetails.getAuthorities())
        );
    }

    /**
     * POST /api/auth/logout
     * ---------------------
     * With JWT, there's no server-side session to destroy.
     * "Logout" means the CLIENT deletes the token from localStorage.
     * This endpoint just confirms the action.
     *
     * INTERVIEW: "How do you invalidate a JWT token?"
     *   True stateless JWT cannot be invalidated server-side.
     *   Options:
     *   1. Short expiry (15 minutes) — accept the risk window
     *   2. Blacklist — store invalidated JWTs in Redis; check on each request
     *   3. Refresh tokens — short-lived access token + long-lived refresh token
     *   For this project, we use approach 1 (24h expiry).
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully. Please clear your token.", null)
        );
    }
}
