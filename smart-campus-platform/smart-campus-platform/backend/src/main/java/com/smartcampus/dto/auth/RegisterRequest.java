package com.smartcampus.dto.auth;

import com.smartcampus.entity.Role;
import jakarta.validation.constraints.*;

/**
 * Self-registration. Students must send department, semester and division;
 * faculty must send department. Admin accounts are created by seeding only.
 */
public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 120, message = "Full name must be between 3 and 120 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
        String password,

        @NotNull(message = "Role is required")
        Role role,

        @NotNull(message = "Department is required")
        Long departmentId,

        // student only
        String rollNumber,
        @Min(value = 1, message = "Semester must be between 1 and 8")
        @Max(value = 8, message = "Semester must be between 1 and 8")
        Integer semester,
        String division,

        // faculty only
        String employeeCode,
        String designation
) {
}
