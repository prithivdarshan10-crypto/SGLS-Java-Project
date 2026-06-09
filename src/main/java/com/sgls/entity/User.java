package com.sgls.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * USER ENTITY
 * -----------
 * Maps to the `users` table in MySQL.
 *
 * Why an Entity class exists:
 *   An @Entity class is a Java object that Hibernate knows how to
 *   save/load from a database table. Every field becomes a column.
 *   Hibernate generates the SQL INSERT/SELECT/UPDATE/DELETE for us.
 *
 * INTERVIEW QUESTION: "What is JPA vs Hibernate?"
 *   JPA (Jakarta Persistence API) is a SPECIFICATION — a set of
 *   interfaces/annotations like @Entity, @Column, @ManyToOne.
 *   Hibernate is the IMPLEMENTATION that actually talks to the DB.
 *   Spring Boot auto-configures Hibernate as the JPA provider.
 */
@Entity
@Table(
    name = "users",
    // Unique constraints prevent duplicate email/username at the DB level
    // (validation at application layer is also needed, but DB is the last guard)
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * PRIMARY KEY
     * @Id marks this as the PK.
     * GenerationType.IDENTITY tells MySQL to auto-increment the value.
     * INTERVIEW: Why IDENTITY over SEQUENCE?
     *   MySQL supports AUTO_INCREMENT natively (IDENTITY).
     *   SEQUENCE is preferred in Oracle/PostgreSQL for batching.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * USERNAME — stored as VARCHAR(50), cannot be null, must be unique.
     * The @Column annotation overrides the default column name and adds DB-level constraints.
     */
    @NotBlank
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /**
     * EMAIL — validated as a proper email format at application layer.
     * The DB only enforces NOT NULL and UNIQUE (via @UniqueConstraint above).
     */
    @NotBlank
    @Email
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * PASSWORD — stored as BCrypt hash (60 chars).
     * NEVER stored in plain text.
     * INTERVIEW: "How does BCrypt work?"
     *   BCrypt applies a slow hashing algorithm with a cost factor (work factor).
     *   Even if the DB is breached, attackers cannot reverse the hash.
     *   Spring's BCryptPasswordEncoder.matches(raw, encoded) does the comparison.
     */
    @NotBlank
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /**
     * FULL NAME — display name, no uniqueness requirement.
     */
    @Column(name = "full_name", length = 100)
    private String fullName;

    /**
     * ROLE — determines what the user can do (RBAC).
     * Stored as a String in DB using @Enumerated(STRING).
     * INTERVIEW: "Why STRING not ORDINAL?"
     *   ORDINAL stores 0, 1, 2... If you ever reorder the enum values,
     *   all existing data breaks silently. STRING stores "ADMIN","MANAGER"
     *   which is safe to reorder or extend.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    /**
     * ACTIVE FLAG — soft disable a user without deleting them.
     * Good practice for audit trails in enterprise systems.
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * AUDIT TIMESTAMPS — automatically set by Hibernate.
     * @CreationTimestamp: set once when the row is first inserted.
     * @UpdateTimestamp: updated every time the row changes.
     * updatable=false on createdAt ensures we can't accidentally overwrite it.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * ROLE ENUM — defined as a nested enum for clean encapsulation.
     * ADMIN   = full system access
     * MANAGER = can manage their warehouse/team
     * EMPLOYEE = read + limited write
     */
    public enum Role {
        ADMIN,
        MANAGER,
        EMPLOYEE
    }
}
