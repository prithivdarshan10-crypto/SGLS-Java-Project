package com.sgls.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * LOGIN REQUEST DTO
 * -----------------
 * DTO = Data Transfer Object.
 *
 * INTERVIEW QUESTION: "Why use DTOs instead of entities in controllers?"
 *
 * 1. SECURITY: If we accepted User entity directly in the controller,
 *    an attacker could POST {"username":"x","password":"y","role":"ADMIN"}
 *    and escalate their privileges. With a DTO, we control exactly what
 *    fields are accepted.
 *
 * 2. DECOUPLING: The API contract (what the client sends) is separate from
 *    the database schema. We can change the DB without breaking the API.
 *
 * 3. VALIDATION: We annotate the DTO fields, not the entity.
 *    @NotBlank, @Email, @Size live here because they describe API input rules.
 *
 * @Data = Lombok shorthand for @Getter + @Setter + @ToString + @EqualsAndHashCode
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
