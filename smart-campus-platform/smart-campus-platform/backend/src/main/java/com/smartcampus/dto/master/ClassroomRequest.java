package com.smartcampus.dto.master;

import jakarta.validation.constraints.*;

public record ClassroomRequest(
        @NotBlank(message = "Room number is required")
        String roomNumber,

        @NotBlank(message = "Building is required")
        String building,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity
) {
}
