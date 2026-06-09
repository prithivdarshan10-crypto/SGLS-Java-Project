package com.sgls.repository;

import com.sgls.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * USER REPOSITORY
 * ---------------
 * The data access layer for the User entity.
 *
 * INTERVIEW QUESTION: "What is Spring Data JPA and JpaRepository?"
 *
 * JpaRepository<T, ID> is a Spring Data interface that gives us
 * 18+ pre-built database methods for FREE without writing any SQL:
 *   - save(entity)        → INSERT or UPDATE
 *   - findById(id)        → SELECT WHERE id = ?
 *   - findAll()           → SELECT *
 *   - delete(entity)      → DELETE
 *   - count()             → SELECT COUNT(*)
 *   - existsById(id)      → SELECT EXISTS(...)
 *   ...and many more
 *
 * We ADD our own custom queries by:
 *   METHOD NAMING: Spring parses method names and generates SQL.
 *     findByEmail(String email) → SELECT * FROM users WHERE email = ?
 *     findByUsernameAndActiveTrue() → SELECT * FROM users WHERE username=? AND is_active=1
 *
 *   @Query: Write JPQL (Java Persistence Query Language) or native SQL.
 *     JPQL uses entity class names, not table names: "FROM User" not "FROM users"
 *
 * INTERVIEW: "What is the difference between JPQL and SQL?"
 *   JPQL operates on entity objects and is database-agnostic.
 *   Native SQL (nativeQuery=true) runs directly in the DB dialect.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * findByUsername — used by UserDetailsServiceImpl for login.
     *
     * METHOD NAMING RULES (Spring Data JPA parses this):
     *   findBy    = SELECT WHERE
     *   Username  = field name in the User entity
     *   Returns Optional<User> to force null-safe handling
     */
    Optional<User> findByUsername(String username);

    /**
     * findByEmail — used during registration to check for duplicate emails.
     */
    Optional<User> findByEmail(String email);

    /**
     * existsByUsername — returns true/false without loading the entity.
     * More efficient than findByUsername().isPresent() because Hibernate
     * generates SELECT EXISTS(...) instead of SELECT *.
     */
    boolean existsByUsername(String username);

    /**
     * existsByEmail — same efficiency gain for email duplicate check.
     */
    boolean existsByEmail(String email);

    /**
     * findByRole — find all users with a specific role.
     * Used by Admin dashboard to list all managers or employees.
     */
    List<User> findByRole(User.Role role);

    /**
     * findByActiveTrue — find only non-disabled users.
     * Method naming: "True" at the end → WHERE is_active = 1
     */
    List<User> findByActiveTrue();

    /**
     * Custom JPQL query — find active users by role.
     * @Query JPQL note: "u.role = :role" uses the enum directly,
     * Hibernate compares by the stored string value ("ADMIN", etc.)
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true")
    List<User> findActiveUsersByRole(User.Role role);
}
