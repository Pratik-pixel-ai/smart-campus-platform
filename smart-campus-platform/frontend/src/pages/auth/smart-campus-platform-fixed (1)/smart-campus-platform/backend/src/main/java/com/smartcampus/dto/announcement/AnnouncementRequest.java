package com.smartcampus.dto.announcement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnouncementRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150)
        String title,

        @NotBlank(message = "Message is required")
        @Size(max = 2000)
        String message,

        /** null means the announcement is campus-wide. */
        Long departmentId,

        Long subjectId,

        @NotBlank(message = "Priority is required")
        String priority
) {
}
