package com.smartcampus.dto.assignment;

import jakarta.validation.constraints.*;

public record AssignmentRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150)
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 2000)
        String description,

        @NotNull(message = "Subject is required")
        Long subjectId,

        @NotBlank(message = "Deadline is required")
        String deadline,

        @NotNull(message = "Maximum marks are required")
        @Min(value = 1, message = "Maximum marks must be at least 1")
        @Max(value = 500)
        Integer maxMarks,

        String attachmentUrl,

        @NotNull(message = "Semester is required")
        @Min(1) @Max(8)
        Integer semester,

        @NotBlank(message = "Division is required")
        String division
) {
}
