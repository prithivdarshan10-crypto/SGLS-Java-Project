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

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        log.info("Login request for user: {}", loginRequest.getUsername());

        JwtResponse jwtResponse = authService.authenticate(loginRequest);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", jwtResponse)
        );
    }

    @PostMapping("/register")
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

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser(
            @org.springframework.security.core.annotation.AuthenticationPrincipal
            org.springframework.security.core.userdetails.UserDetails userDetails) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current user: " + userDetails.getUsername()
                                + " | Role: " + userDetails.getAuthorities()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logged out successfully. Please clear your token.",
                        null
                )
        );
    }
}
