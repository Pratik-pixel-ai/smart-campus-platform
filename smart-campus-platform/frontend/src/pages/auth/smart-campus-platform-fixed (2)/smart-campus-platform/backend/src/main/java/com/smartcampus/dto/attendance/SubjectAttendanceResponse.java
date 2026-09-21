package com.smartcampus.dto.attendance;

public record SubjectAttendanceResponse(
        Long subjectId,
        String subjectName,
        String subjectCode,
        long totalLectures,
        long presentLectures,
        long absentLectures,
        double percentage
) {
}
