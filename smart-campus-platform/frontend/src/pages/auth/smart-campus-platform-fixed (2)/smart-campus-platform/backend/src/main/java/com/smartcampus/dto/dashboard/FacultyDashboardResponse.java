package com.smartcampus.dto.dashboard;

import com.smartcampus.dto.announcement.AnnouncementResponse;
import com.smartcampus.dto.attendance.AttendanceSessionResponse;
import com.smartcampus.dto.timetable.TimetableResponse;

import java.util.List;

public record FacultyDashboardResponse(
        String facultyName,
        String departmentName,
        String designation,
        int subjectsTaught,
        long totalStudents,
        long sessionsToday,
        long pendingReviews,
        List<TimetableResponse> todayClasses,
        List<AttendanceSessionResponse> recentSessions,
        List<AnnouncementResponse> recentAnnouncements
) {
}
