package com.sgls.service;

import com.sgls.dto.request.LoginRequest;
import com.sgls.dto.request.RegisterRequest;
import com.sgls.dto.response.JwtResponse;
import com.sgls.entity.User;
import com.sgls.exception.BadRequestException;
import com.sgls.repository.UserRepository;
import com.sgls.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AUTHENTICATION SERVICE
 * ----------------------
 * Contains ALL business logic for authentication.
 *
 * INTERVIEW QUESTION: "Why separate Service from Controller?"
 *
 * The Controller's job: handle HTTP (request/response mapping, status codes).
 * The Service's job: business logic (validate, process, persist).
 *
 * Benefits of separation:
 *   1. TESTABILITY: Services can be unit-tested without HTTP overhead.
 *   2. REUSABILITY: Service methods can be called from different controllers.
 *   3. TRANSACTION MANAGEMENT: @Transactional goes on service, not controller.
 *   4. SINGLE RESPONSIBILITY: Each class has one job.
 *
 * @Slf4j — Lombok generates: private static final Logger log = LoggerFactory.getLogger(...)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * authenticate — handles user login.
     *
     * STEP-BY-STEP (critical for interviews):
     *
     * 1. Create an UnAuthenticatedToken with username + password.
     *    This is an "I want to authenticate" request object.
     *
     * 2. authenticationManager.authenticate() does:
     *    a. Calls userDetailsService.loadUserByUsername(username)
     *    b. Gets the stored BCrypt hash
     *    c. Calls passwordEncoder.matches(rawPassword, bcryptHash)
     *    d. If mismatch → throws BadCredentialsException → 401
     *    e. If match → returns an Authenticated token
     *
     * 3. Store authenticated result in SecurityContextHolder.
     *    This makes the user "logged in" for the duration of this request.
     *
     * 4. Generate a JWT with jwtUtils.generateJwtToken().
     *
     * 5. Load user from DB to include extra info in the response.
     *
     * 6. Return JwtResponse DTO with token + user info.
     *
     * @Transactional(readOnly=true) — we only READ the DB here, no writes.
     *   readOnly=true is a performance hint to Hibernate (skips dirty checking).
     */
    @Transactional(readOnly = true)
    public JwtResponse authenticate(LoginRequest loginRequest) {
        log.info("Authentication attempt for user: {}", loginRequest.getUsername());

        // Step 1+2: Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Step 3: Set in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 4: Generate JWT
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Step 5: Get user details for the response
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        log.info("Authentication successful for user: {}", user.getUsername());

        // Step 6: Build and return the response
        return JwtResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .expiresIn(jwtExpirationMs)
                .build();
    }

    /**
     * register — creates a new user account.
     *
     * Only callable by ADMIN (enforced at controller level with @PreAuthorize).
     *
     * Business rules:
     *   1. Username must be unique
     *   2. Email must be unique
     *   3. Password is hashed with BCrypt before storing
     *   4. Role is taken from the request
     *
     * @Transactional — if any step fails, the entire DB write is rolled back.
     */
    @Transactional
    public User register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Validation: check for duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        // Validation: check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use: " + request.getEmail());
        }

        // Build the User entity from the DTO
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt hash!
                .fullName(request.getFullName())
                .role(request.getRole())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully with ID: {}", saved.getId());

        return saved;
    }
}
