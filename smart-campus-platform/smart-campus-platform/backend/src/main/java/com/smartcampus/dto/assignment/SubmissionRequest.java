package com.smartcampus.dto.assignment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmissionRequest(
        @NotNull(message = "Assignment is required")
        Long assignmentId,

        @NotBlank(message = "Submission link is required")
        @Size(max = 500)
        String submissionUrl,

        @Size(max = 500)
        String remarks
) {
}
