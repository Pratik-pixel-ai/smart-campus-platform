package com.smartcampus.dto.dashboard;

import com.smartcampus.dto.announcement.AnnouncementResponse;

import java.util.List;

public record AdminDashboardResponse(
        long totalStudents,
        long totalFaculty,
        long totalDepartments,
        long totalSubjects,
        long totalSessions,
        long openSessions,
        double campusAttendancePercentage,
        long totalAssignments,
        long totalSubmissions,
        List<DepartmentStat> departmentStats,
        List<AnnouncementResponse> recentAnnouncements
) {
    public record DepartmentStat(String department, long students, long faculty, double attendancePercentage) {
    }
}
