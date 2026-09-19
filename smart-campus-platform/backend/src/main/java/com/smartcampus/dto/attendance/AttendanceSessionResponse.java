package com.smartcampus.dto.attendance;

import java.util.List;

public record AttendanceSessionResponse(
        Long id,
        Long subjectId,
        String subjectName,
        String subjectCode,
        String facultyName,
        String room,
        String sessionDate,
        Integer lectureNumber,
        Integer durationMinutes,
        Integer semester,
        String division,
        String status,
        String mode,
        String startedAt,
        String closedAt,
        int enrolledCount,
        int presentCount,
        int absentCount,
        List<AttendanceRecordResponse> records
) {
}
