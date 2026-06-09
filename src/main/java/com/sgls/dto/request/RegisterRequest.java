package com.sgls.dto.request;

import com.sgls.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * REGISTER REQUEST DTO
 * --------------------
 * Contains all data needed to create a new user account.
 * Only ADMIN can call the register endpoint (enforced in controller).
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be 6-40 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    /**
     * role — which role to assign. Defaults to EMPLOYEE if not specified.
     * The controller validates that only ADMIN can create ADMIN accounts.
     */
    private User.Role role = User.Role.EMPLOYEE;
}
