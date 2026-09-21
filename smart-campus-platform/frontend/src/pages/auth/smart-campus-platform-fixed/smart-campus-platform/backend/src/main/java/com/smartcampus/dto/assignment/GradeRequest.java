package com.smartcampus.dto.assignment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GradeRequest(
        @NotNull(message = "Marks are required")
        @Min(value = 0, message = "Marks cannot be negative")
        Integer marksObtained,

        @Size(max = 1000)
        String feedback
) {
}
