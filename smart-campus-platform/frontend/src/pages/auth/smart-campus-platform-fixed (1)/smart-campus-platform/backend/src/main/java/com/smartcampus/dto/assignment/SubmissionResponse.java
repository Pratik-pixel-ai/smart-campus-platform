package com.smartcampus.dto.assignment;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        Integer maxMarks,
        Long studentId,
        String studentName,
        String rollNumber,
        String submissionUrl,
        String remarks,
        String submittedAt,
        String status,
        Integer marksObtained,
        String feedback,
        String gradedAt
) {
}
