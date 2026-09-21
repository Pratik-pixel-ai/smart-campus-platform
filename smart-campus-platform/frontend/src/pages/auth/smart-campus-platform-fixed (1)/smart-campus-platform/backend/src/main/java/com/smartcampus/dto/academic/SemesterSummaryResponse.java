package com.smartcampus.dto.academic;

import java.util.List;

public record SemesterSummaryResponse(
        Integer semester,
        int subjectCount,
        int totalMarks,
        int maxPossible,
        double percentage,
        List<AcademicRecordResponse> records
) {
}
