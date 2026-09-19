package com.smartcampus.dto.dashboard;

import com.smartcampus.dto.announcement.AnnouncementResponse;
import com.smartcampus.dto.assignment.AssignmentResponse;
import com.smartcampus.dto.attendance.AttendanceSummaryResponse;
import com.smartcampus.dto.timetable.TimetableResponse;

import java.util.List;

public record StudentDashboardResponse(
        String studentName,
        String rollNumber,
        String departmentName,
        Integer semester,
        String division,
        AttendanceSummaryResponse attendance,
        List<TimetableResponse> todayClasses,
        List<AssignmentResponse> pendingAssignments,
        List<AnnouncementResponse> recentAnnouncements,
        double academicPercentage,
        int subjectsGraded
) {
}
