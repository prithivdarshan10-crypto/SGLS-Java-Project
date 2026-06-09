package com.sgls.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT RESPONSE DTO
 * ----------------
 * What we send back to the client after a successful login.
 *
 * The client stores this token (usually in localStorage or a cookie)
 * and sends it in every subsequent request:
 *   Authorization: Bearer <token>
 *
 * INTERVIEW: "What should a login response contain?"
 *   Minimum: the JWT token and its type ("Bearer").
 *   Optional but useful: username, role, expiry — so the frontend
 *   can display the user's name and show/hide role-specific UI
 *   WITHOUT making an extra API call.
 *
 *   We include expiry so the frontend can show a "session expiring"
 *   warning before the token actually expires.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Long expiresIn; // milliseconds until expiry
}
