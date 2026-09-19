package com.smartcampus.dto.attendance;

public record AttendanceRecordResponse(
        Long id,
        Long studentId,
        String studentName,
        String rollNumber,
        String status,
        String detectionMethod,
        Integer signalStrength,
        String markedAt,
        String subjectName,
        String sessionDate
) {
}
