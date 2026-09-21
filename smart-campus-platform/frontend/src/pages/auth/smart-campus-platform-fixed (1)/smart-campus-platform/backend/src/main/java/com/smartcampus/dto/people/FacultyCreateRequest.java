package com.smartcampus.dto.people;

import jakarta.validation.constraints.*;

public record FacultyCreateRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Employee code is required")
        String employeeCode,

        @NotNull(message = "Department is required")
        Long departmentId,

        String designation,
        String phone
) {
}
