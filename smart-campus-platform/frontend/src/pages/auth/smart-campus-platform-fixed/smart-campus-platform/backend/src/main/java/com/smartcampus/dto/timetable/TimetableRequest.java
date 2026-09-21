package com.smartcampus.dto.timetable;

import jakarta.validation.constraints.*;

public record TimetableRequest(
        @NotNull(message = "Subject is required")
        Long subjectId,

        @NotNull(message = "Faculty is required")
        Long facultyId,

        Long classroomId,

        @NotBlank(message = "Day is required")
        String dayOfWeek,

        @NotBlank(message = "Start time is required")
        String startTime,

        @NotBlank(message = "End time is required")
        String endTime,

        @NotNull(message = "Semester is required")
        @Min(1) @Max(8)
        Integer semester,

        @NotBlank(message = "Division is required")
        String division
) {
}
