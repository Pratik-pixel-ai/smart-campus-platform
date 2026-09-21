package com.smartcampus.dto.master;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Department name is required")
        @Size(max = 100)
        String name,

        @NotBlank(message = "Department code is required")
        @Size(max = 10, message = "Code can be at most 10 characters")
        String code,

        String hodName
) {
}
