package com.sgls.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT AUTHENTICATION FILTER
 * --------------------------
 * This filter runs ONCE per HTTP request (OncePerRequestFilter).
 * It intercepts every incoming request BEFORE it reaches any controller.
 *
 * INTERVIEW QUESTION: "How does JWT authentication work in Spring Security?"
 *
 * The FILTER CHAIN is Spring Security's pipeline:
 *   Request → [Filter1] → [Filter2] → [AuthTokenFilter] → [Controller]
 *
 * Our filter:
 *   1. Reads the Authorization header
 *   2. Extracts the JWT token
 *   3. Validates it with JwtUtils
 *   4. Loads the user from DB
 *   5. Sets the Authentication in SecurityContext
 *
 * After this, Spring Security knows WHO is making the request.
 * The SecurityContext is stored per-thread (ThreadLocal).
 *
 * INTERVIEW: "What is SecurityContextHolder?"
 *   It's a thread-local storage that holds the current user's
 *   Authentication object for the duration of the request.
 *   Every @PreAuthorize or .hasRole() check reads from here.
 */
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * doFilterInternal — the core logic executed for every HTTP request.
     *
     * @param request     — incoming HTTP request
     * @param response    — outgoing HTTP response
     * @param filterChain — the rest of the filter pipeline; we call
     *                      chain.doFilter() to pass the request forward.
     *
     * IMPORTANT: We always call filterChain.doFilter() even when
     * validation fails. Spring Security's ExceptionTranslationFilter
     * will handle the 401 Unauthorized if SecurityContext is empty.
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Step 1: Extract the JWT from the "Authorization: Bearer <token>" header
            String jwt = parseJwt(request);

            // Step 2: Validate the token (not null, not expired, signature matches)
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {

                // Step 3: Extract username from the token payload
                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                // Step 4: Load user details from the database
                // This gives us roles, password (not needed here), active status etc.
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 5: Build a Spring Security Authentication object
                // UsernamePasswordAuthenticationToken(principal, credentials, authorities)
                // credentials = null because JWT-authenticated users don't need password re-check
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()  // ROLE_ADMIN, ROLE_MANAGER, etc.
                        );

                // Attach request details (IP address, session ID) for audit logging
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 6: Store in SecurityContext — Spring Security now knows who this is
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // ALWAYS continue the filter chain, even on failure
        filterChain.doFilter(request, response);
    }

    /**
     * parseJwt — extracts the token string from the Authorization header.
     *
     * The standard format is:
     *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1...
     *
     * We check:
     *   1. Header exists (StringUtils.hasText)
     *   2. Header starts with "Bearer " (not "Basic " etc.)
     *   3. Substring after "Bearer " is the token
     *
     * Returns null if no valid Authorization header is found.
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // "Bearer " is 7 characters
        }

        return null;
    }
}
