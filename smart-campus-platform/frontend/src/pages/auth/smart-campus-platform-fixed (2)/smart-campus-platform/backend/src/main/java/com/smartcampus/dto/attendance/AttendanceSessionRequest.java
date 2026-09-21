package com.smartcampus.dto.attendance;

import jakarta.validation.constraints.*;

public record AttendanceSessionRequest(
        @NotNull(message = "Subject is required")
        Long subjectId,

        Long classroomId,

        /** Defaults to today when omitted. */
        String sessionDate,

        @NotNull(message = "Lecture number is required")
        @Min(1) @Max(12)
        Integer lectureNumber,

        @NotNull(message = "Duration is required")
        @Min(value = 5, message = "Duration must be at least 5 minutes")
        @Max(value = 240, message = "Duration cannot exceed 240 minutes")
        Integer durationMinutes,

        @NotNull(message = "Semester is required")
        @Min(1) @Max(8)
        Integer semester,

        @NotBlank(message = "Division is required")
        String division
) {
}
