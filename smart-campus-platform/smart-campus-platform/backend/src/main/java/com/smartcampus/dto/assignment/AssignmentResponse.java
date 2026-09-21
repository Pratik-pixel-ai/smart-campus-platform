package com.smartcampus.dto.assignment;

public record AssignmentResponse(
        Long id,
        String title,
        String description,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long facultyId,
        String facultyName,
        String deadline,
        Integer maxMarks,
        String attachmentUrl,
        Integer semester,
        String division,
        long submissionCount,
        /** Filled only when a student asks for the list. */
        String submissionStatus,
        Integer marksObtained,
        boolean overdue
) {
}
