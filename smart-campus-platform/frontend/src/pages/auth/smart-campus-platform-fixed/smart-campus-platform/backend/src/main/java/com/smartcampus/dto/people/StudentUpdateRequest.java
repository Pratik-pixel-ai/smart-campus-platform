package com.smartcampus.dto.people;

import jakarta.validation.constraints.*;

public record StudentUpdateRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotNull(message = "Department is required")
        Long departmentId,

        @NotNull(message = "Semester is required")
        @Min(1) @Max(8)
        Integer semester,

        @NotBlank(message = "Division is required")
        String division,

        String phone,
        String bleDeviceId,
        Boolean active
) {
}
