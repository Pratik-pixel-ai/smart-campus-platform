package com.smartcampus.dto.academic;

public record AcademicRecordResponse(
        Long id,
        Long studentId,
        String studentName,
        String rollNumber,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Integer semester,
        Integer internalMarks,
        Integer externalMarks,
        Integer totalMarks,
        String grade
) {
}
