package com.smartcampus.dto.attendance;

import java.util.List;

public record AttendanceSummaryResponse(
        Long studentId,
        String studentName,
        String rollNumber,
        long totalLectures,
        long presentLectures,
        long absentLectures,
        double overallPercentage,
        /** How many more lectures can be missed before dropping under 75%. */
        long lecturesCanMiss,
        List<SubjectAttendanceResponse> subjects
) {
}
