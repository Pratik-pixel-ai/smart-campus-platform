package com.smartcampus.dto.master;

import jakarta.validation.constraints.*;

public record SubjectRequest(
        @NotBlank(message = "Subject name is required")
        String name,

        @NotBlank(message = "Subject code is required")
        String code,

        @NotNull(message = "Department is required")
        Long departmentId,

        Long facultyId,

        @NotNull(message = "Semester is required")
        @Min(1) @Max(8)
        Integer semester,

        @NotNull(message = "Credits are required")
        @Min(1) @Max(10)
        Integer credits
) {
}
