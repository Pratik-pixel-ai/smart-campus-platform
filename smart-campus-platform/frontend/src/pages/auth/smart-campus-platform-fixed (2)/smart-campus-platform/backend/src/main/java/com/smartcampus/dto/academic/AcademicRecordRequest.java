package com.smartcampus.dto.academic;

import jakarta.validation.constraints.*;

public record AcademicRecordRequest(
        @NotNull(message = "Student is required")
        Long studentId,

        @NotNull(message = "Subject is required")
        Long subjectId,

        @NotNull(message = "Semester is required")
        @Min(1) @Max(8)
        Integer semester,

        @NotNull(message = "Internal marks are required")
        @Min(0) @Max(40)
        Integer internalMarks,

        @NotNull(message = "External marks are required")
        @Min(0) @Max(60)
        Integer externalMarks
) {
}
