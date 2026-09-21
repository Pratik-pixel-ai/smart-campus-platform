package com.smartcampus.service;

import com.smartcampus.dto.academic.AcademicSummaryResponse;
import com.smartcampus.dto.announcement.AnnouncementResponse;
import com.smartcampus.dto.assignment.AssignmentResponse;
import com.smartcampus.dto.attendance.AttendanceSummaryResponse;
import com.smartcampus.dto.attendance.SubjectAttendanceResponse;
import com.smartcampus.dto.timetable.TimetableResponse;
import com.smartcampus.entity.Role;
import com.smartcampus.entity.User;
import com.smartcampus.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Builds the small, role-appropriate snapshot of campus data that is sent to the AI
 * provider with the user's question.
 *
 * This is the whole safety story for the assistant: the model never touches the
 * database, it only ever sees the text assembled here, and a student's snapshot is
 * built from their own JWT identity - never from an id supplied by the browser.
 */
@Service
@RequiredArgsConstructor
public class CampusContextService {

    private static final int MAX_ASSIGNMENTS = 5;
    private static final int MAX_ANNOUNCEMENTS = 3;

    private final CurrentUser currentUser;
    private final AttendanceQueryService attendanceQueryService;
    private final TimetableService timetableService;
    private final AssignmentService assignmentService;
    private final AnnouncementService announcementService;
    private final AcademicService academicService;

    @Transactional(readOnly = true)
    public String buildContext() {
        User user = currentUser.user();
        StringBuilder context = new StringBuilder();
        context.append("User: ").append(user.getFullName())
                .append(" (").append(user.getRole().name().replace("ROLE_", "").toLowerCase()).append(")\n");
        context.append("Today: ").append(java.time.LocalDate.now()).append(" (")
                .append(java.time.LocalDate.now().getDayOfWeek()).append(")\n\n");

        if (user.getRole() == Role.ROLE_STUDENT) {
            appendStudentContext(context);
        } else {
            appendStaffContext(context);
        }
        return context.toString();
    }

    private void appendStudentContext(StringBuilder context) {
        AttendanceSummaryResponse attendance = attendanceQueryService.mySummary();
        context.append("ATTENDANCE\n");
        context.append("Overall: ").append(attendance.overallPercentage()).append("% (")
                .append(attendance.presentLectures()).append(" present of ")
                .append(attendance.totalLectures()).append(" lectures). ")
                .append("Lectures that can still be missed while staying at 75%: ")
                .append(attendance.lecturesCanMiss()).append(".\n");
        for (SubjectAttendanceResponse subject : attendance.subjects()) {
            context.append("- ").append(subject.subjectName()).append(" (").append(subject.subjectCode()).append("): ")
                    .append(subject.percentage()).append("% (")
                    .append(subject.presentLectures()).append("/").append(subject.totalLectures()).append(")\n");
        }

        context.append("\nTODAY'S CLASSES\n");
        List<TimetableResponse> today = timetableService.myToday();
        if (today.isEmpty()) {
            context.append("No lectures scheduled today.\n");
        } else {
            today.forEach(entry -> context.append("- ").append(entry.startTime()).append("-").append(entry.endTime())
                    .append(" ").append(entry.subjectName()).append(" in ").append(entry.room())
                    .append(" with ").append(entry.facultyName()).append("\n"));
        }

        context.append("\nUPCOMING ASSIGNMENTS\n");
        List<AssignmentResponse> assignments = assignmentService.listForCurrentUser().stream()
                .filter(a -> !a.overdue() || "PENDING".equals(a.submissionStatus()))
                .limit(MAX_ASSIGNMENTS)
                .toList();
        if (assignments.isEmpty()) {
            context.append("Nothing pending.\n");
        } else {
            assignments.forEach(a -> context.append("- ").append(a.title()).append(" (").append(a.subjectName())
                    .append("), due ").append(a.deadline()).append(", status ").append(a.submissionStatus()).append("\n"));
        }

        AcademicSummaryResponse academics = academicService.mySummary();
        context.append("\nACADEMIC RECORD\nOverall: ").append(academics.overallPercentage()).append("%\n");
        academics.semesters().forEach(sem -> context.append("- Semester ").append(sem.semester()).append(": ")
                .append(sem.percentage()).append("% across ").append(sem.subjectCount()).append(" subjects\n"));

        appendAnnouncements(context);
    }

    private void appendStaffContext(StringBuilder context) {
        context.append("TODAY'S CLASSES\n");
        List<TimetableResponse> today = timetableService.myToday();
        if (today.isEmpty()) {
            context.append("No lectures scheduled today.\n");
        } else {
            today.forEach(entry -> context.append("- ").append(entry.startTime()).append("-").append(entry.endTime())
                    .append(" ").append(entry.subjectName()).append(" (sem ").append(entry.semester())
                    .append(" div ").append(entry.division()).append(") in ").append(entry.room()).append("\n"));
        }

        context.append("\nASSIGNMENTS\n");
        List<AssignmentResponse> assignments = assignmentService.listForCurrentUser().stream()
                .limit(MAX_ASSIGNMENTS).toList();
        if (assignments.isEmpty()) {
            context.append("None created yet.\n");
        } else {
            assignments.forEach(a -> context.append("- ").append(a.title()).append(" (").append(a.subjectName())
                    .append("), due ").append(a.deadline()).append(", ").append(a.submissionCount())
                    .append(" submissions\n"));
        }

        context.append("\nCAMPUS ATTENDANCE\nOverall recorded attendance: ")
                .append(attendanceQueryService.campusPercentage()).append("%\n");

        appendAnnouncements(context);
    }

    private void appendAnnouncements(StringBuilder context) {
        context.append("\nRECENT ANNOUNCEMENTS\n");
        List<AnnouncementResponse> announcements = announcementService.recent(MAX_ANNOUNCEMENTS);
        if (announcements.isEmpty()) {
            context.append("None.\n");
        } else {
            announcements.forEach(a -> context.append("- [").append(a.priority()).append("] ")
                    .append(a.title()).append(": ").append(a.message()).append("\n"));
        }
    }
}
