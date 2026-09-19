package com.smartcampus.dto.people;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FacultyUpdateRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotNull(message = "Department is required")
        Long departmentId,

        String designation,
        String phone,
        Boolean active
) {
}
