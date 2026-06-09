package com.sgls.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT UTILITY CLASS
 * -----------------
 * Handles all JWT operations: generation, validation, extraction.
 *
 * INTERVIEW QUESTION: "What is a JWT and how does it work?"
 * A JWT (JSON Web Token) has 3 parts separated by dots:
 *   HEADER.PAYLOAD.SIGNATURE
 *
 *   Header  = algorithm type (e.g., HS256)
 *   Payload = claims (username, role, expiry) — base64 encoded, NOT encrypted
 *   Signature = HMAC(header + payload, secret_key)
 *
 * The server signs the token with a secret key.
 * On every request, the server re-computes the signature and checks it matches.
 * If the token was tampered with, the signature won't match → rejected.
 *
 * INTERVIEW: "Why JWT over sessions?"
 *   Sessions are stored server-side (memory or DB) — stateful.
 *   JWT is stateless — the token itself carries all needed info.
 *   For microservices and horizontal scaling, stateless is preferred.
 *
 * @Component makes this a Spring-managed bean so we can @Autowire it.
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * Secret key — injected from application.properties.
     * In production, this comes from Render's environment variables.
     * @Value("${app.jwt.secret}") reads: "look for property key app.jwt.secret"
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Token validity in milliseconds (24 hours default).
     */
    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * generateJwtToken — creates a signed JWT for a logged-in user.
     *
     * @param authentication — Spring Security's Authentication object,
     *        which contains the UserDetails of the logged-in user.
     *
     * How it works:
     *   1. Extract the UserDetails (username) from Authentication
     *   2. Build a JWT with subject = username
     *   3. Set issued-at = now, expiration = now + 24h
     *   4. Sign with HS256 algorithm using our secret key
     *
     * INTERVIEW: "What is HS256?"
     *   HMAC-SHA256 — symmetric algorithm. Same key is used to sign AND verify.
     *   Alternative: RS256 (asymmetric) — private key signs, public key verifies.
     *   RS256 is better for microservices where services only need to verify.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())        // who the token is about
                .setIssuedAt(new Date())                        // when it was created
                .setExpiration(new Date(                        // when it expires
                        System.currentTimeMillis() + jwtExpirationMs
                ))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // SIGN IT
                .compact();                                     // serialize to string
    }

    /**
     * generateTokenFromUsername — generates a JWT directly from a username string.
     * Used when we need to generate a token without a full Authentication object
     * (e.g., after password reset or email verification flows).
     */
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * getUserNameFromJwtToken — extracts the subject (username) from a token.
     *
     * .parseClaimsJws() — parses AND validates the signature.
     * .getBody()        — gives us the Claims (payload).
     * .getSubject()     — returns the "sub" field we set during generation.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * validateJwtToken — returns true if the token is valid, false otherwise.
     *
     * We catch specific exceptions to give meaningful log messages:
     *   SignatureException     — token was tampered with
     *   MalformedJwtException  — not a valid JWT format
     *   ExpiredJwtException    — token has expired (user must re-login)
     *   UnsupportedJwtException— unexpected algorithm or format
     *   IllegalArgumentException — empty string or null passed
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * getSigningKey — converts the plain-text secret from config into a
     * cryptographic Key object suitable for signing.
     *
     * Why base64 decode?
     *   Keys.hmacShaKeyFor() requires raw bytes.
     *   We store the secret as a base64 string in config for safe transport.
     *
     * INTERVIEW: "What minimum length is required for HS256?"
     *   HS256 needs at least 256 bits = 32 bytes.
     *   Our default secret is already > 32 chars, so it's safe.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
