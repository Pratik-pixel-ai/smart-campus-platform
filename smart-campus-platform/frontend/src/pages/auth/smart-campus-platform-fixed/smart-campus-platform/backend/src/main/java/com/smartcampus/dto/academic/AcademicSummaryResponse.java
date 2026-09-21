package com.smartcampus.dto.academic;

import java.util.List;

public record AcademicSummaryResponse(
        Long studentId,
        String studentName,
        String rollNumber,
        double overallPercentage,
        List<SemesterSummaryResponse> semesters
) {
}
