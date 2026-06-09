package com.sgls.config;

import com.sgls.security.AuthTokenFilter;
import com.sgls.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * WEB SECURITY CONFIGURATION
 * ---------------------------
 * The central security configuration class for the entire application.
 *
 * INTERVIEW QUESTION: "How does Spring Security work?"
 * Spring Security is a chain of Servlet filters.
 * Before a request reaches your controller, it passes through filters:
 *   [CORS] → [CSRF] → [AuthTokenFilter (ours)] → [ExceptionTranslation] → [Controller]
 *
 * This class defines:
 *   1. Which routes are public vs protected
 *   2. How authentication works (our DaoAuthenticationProvider + BCrypt)
 *   3. Session management (STATELESS for JWT)
 *   4. Where our custom JWT filter plugs in
 *   5. Method-level security (@PreAuthorize)
 *
 * @Configuration — tells Spring this class provides @Bean definitions.
 * @EnableWebSecurity — activates Spring Security's web security support.
 * @EnableMethodSecurity — enables @PreAuthorize, @Secured on methods.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    /**
     * authenticationJwtTokenFilter — creates our custom JWT filter bean.
     *
     * Why not just @Component on AuthTokenFilter?
     *   Declaring it as a @Bean here gives us full control over its
     *   lifecycle. If it were just @Component, Spring might register it
     *   in the filter chain TWICE (once from @Component scan, once from
     *   our manual addFilterBefore call). This avoids that bug.
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * passwordEncoder — BCrypt password hashing.
     *
     * Why BCrypt?
     *   It's adaptive (you can increase work factor as hardware gets faster).
     *   It's salted (each hash is unique even for the same password).
     *   It's slow by design (brute-force resistant).
     *
     * strength=10 means 2^10 = 1024 rounds. Default and recommended.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * authenticationProvider — connects our UserDetailsService + PasswordEncoder
     * to Spring Security's authentication mechanism.
     *
     * DaoAuthenticationProvider (Data Access Object):
     *   1. Calls userDetailsService.loadUserByUsername(username)
     *   2. Calls passwordEncoder.matches(rawPassword, encodedPassword)
     *   3. If both pass → authentication succeeds
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * authenticationManager — the master authentication coordinator.
     *
     * Spring Security's AuthenticationManager.authenticate(token) is
     * what our AuthController calls during login.
     * It delegates to the DaoAuthenticationProvider we configured above.
     *
     * We get it from AuthenticationConfiguration (auto-configured by Spring Boot)
     * to avoid circular dependency issues.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * filterChain — THE MAIN SECURITY CONFIGURATION.
     *
     * This is the most important method to understand for interviews.
     * It configures the entire HTTP security pipeline.
     *
     * @param http — Spring Security's HttpSecurity builder (fluent API)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF (Cross-Site Request Forgery) protection:
            // Disabled because JWT-based APIs are inherently CSRF-safe.
            // JWT tokens are sent in Authorization header, not cookies,
            // so a malicious cross-site form cannot trigger them.
            .csrf(csrf -> csrf.disable())

            // CORS (Cross-Origin Resource Sharing):
            // Disabled here for simplicity; in production, configure
            // allowed origins, methods, and headers explicitly.
            .cors(cors -> cors.disable())

            // AUTHORIZATION — which endpoints require which roles:
            .authorizeHttpRequests(auth -> auth

                // PUBLIC endpoints — no authentication required
                .requestMatchers(
                    "/api/auth/**",    // Login, register
                    "/",               // Landing page
                    "/login",          // Login form
                    "/css/**",         // Static CSS
                    "/js/**",          // Static JS
                    "/images/**",      // Static images
                    "/error"           // Error page
                ).permitAll()

                // ADMIN-ONLY endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // MANAGER and above
                .requestMatchers("/api/warehouse/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/api/employees/**").hasAnyRole("ADMIN", "MANAGER")

                // AUTHENTICATED users (any role)
                .requestMatchers("/api/inventory/**").authenticated()
                .requestMatchers("/api/shipments/**").authenticated()
                .requestMatchers("/api/suppliers/**").authenticated()
                .requestMatchers("/api/analytics/**").authenticated()
                .requestMatchers("/dashboard/**").authenticated()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // SESSION MANAGEMENT — STATELESS for JWT:
            // INTERVIEW: "Why STATELESS?"
            //   With JWTs, the server doesn't store session state.
            //   Every request is self-contained — the JWT carries the
            //   user's identity. This enables horizontal scaling:
            //   any server instance can handle any request.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Register our DaoAuthenticationProvider
            .authenticationProvider(authenticationProvider())

            // Add our JWT filter BEFORE the standard
            // UsernamePasswordAuthenticationFilter.
            // Why before? Because we want JWT check to happen first.
            // If JWT is valid, we set SecurityContext and skip form login.
            .addFilterBefore(
                authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
