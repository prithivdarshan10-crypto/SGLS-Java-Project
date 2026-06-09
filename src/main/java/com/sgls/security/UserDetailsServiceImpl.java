package com.sgls.security;

import com.sgls.entity.User;
import com.sgls.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * USER DETAILS SERVICE IMPLEMENTATION
 * ------------------------------------
 * Spring Security requires a UserDetailsService to know HOW to load
 * a user by username from our specific database/data source.
 *
 * INTERVIEW QUESTION: "What is UserDetailsService?"
 *   It's a Spring Security interface with ONE method:
 *     UserDetails loadUserByUsername(String username)
 *   Spring Security calls this during authentication to:
 *     1. Load the user from wherever we store them (DB, LDAP, etc.)
 *     2. Get their hashed password (to compare with what they typed)
 *     3. Get their authorities (roles/permissions)
 *
 * By implementing this interface, we bridge OUR User entity with
 * Spring Security's expectation of a UserDetails object.
 *
 * @Service — marks this as a Spring bean (service layer component).
 * @RequiredArgsConstructor — Lombok generates constructor injection for
 *   all final fields (avoids @Autowired field injection anti-pattern).
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * loadUserByUsername — the single required method of UserDetailsService.
     *
     * @param username — the username from the login request
     * @return UserDetails — Spring Security's user representation
     * @throws UsernameNotFoundException — if user doesn't exist
     *
     * @Transactional ensures the database session stays open while
     * Hibernate loads the entity (avoids LazyInitializationException
     * on lazily-loaded collections).
     *
     * How we bridge User → UserDetails:
     *   Spring's org.springframework.security.core.userdetails.User
     *   (not our entity) implements UserDetails. We build one using
     *   its builder with our entity's data.
     *
     * INTERVIEW: "What is the difference between your User entity
     * and Spring Security's User class?"
     *   Our entity: JPA-mapped, has business fields (fullName, role, etc.)
     *   Spring's User: implements UserDetails, understood by the security
     *   framework. We convert between them here.
     *
     * AUTHORITY FORMAT: Spring Security expects roles prefixed with
     * "ROLE_" for .hasRole("ADMIN") checks.
     *   hasRole("ADMIN")    → looks for authority "ROLE_ADMIN"
     *   hasAuthority("ROLE_ADMIN") → exact match
     *
     * We map our enum Role.ADMIN → "ROLE_ADMIN" via:
     *   "ROLE_" + user.getRole().name()
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user in DB — throw if not found
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username)
                );

        // Check if the user account is active (soft-delete / disable support)
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }

        // Build Spring Security's UserDetails from our entity
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())  // BCrypt hash
                .authorities(
                        // Single role per user in this system (simplest RBAC model)
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                        )
                )
                .accountExpired(false)     // We don't expire accounts
                .accountLocked(false)      // We use isActive flag instead
                .credentialsExpired(false) // We don't rotate passwords on schedule
                .disabled(!user.isActive())
                .build();
    }
}
