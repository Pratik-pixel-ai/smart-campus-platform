package com.smartcampus.dto.people;

import jakarta.validation.constraints.*;

public record StudentCreateRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 120)
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Roll number is required")
        String rollNumber,

        @NotNull(message = "Department is required")
        Long departmentId,

        @NotNull(message = "Semester is required")
        @Min(1) @Max(8)
        Integer semester,

        @NotBlank(message = "Division is required")
        String division,

        String phone,
        String bleDeviceId
) {
}
