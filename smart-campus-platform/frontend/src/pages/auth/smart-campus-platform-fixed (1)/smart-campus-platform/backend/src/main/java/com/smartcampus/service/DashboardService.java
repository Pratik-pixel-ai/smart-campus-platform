package com.smartcampus.service;

import com.smartcampus.dto.announcement.AnnouncementResponse;
import com.smartcampus.dto.assignment.AssignmentResponse;
import com.smartcampus.dto.attendance.AttendanceSessionResponse;
import com.smartcampus.dto.attendance.AttendanceSummaryResponse;
import com.smartcampus.dto.dashboard.AdminDashboardResponse;
import com.smartcampus.dto.dashboard.FacultyDashboardResponse;
import com.smartcampus.dto.dashboard.StudentDashboardResponse;
import com.smartcampus.dto.timetable.TimetableResponse;
import com.smartcampus.entity.AttendanceStatus;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.SessionStatus;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.SubmissionStatus;
import com.smartcampus.repository.*;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Assembles the three role dashboards. Each dashboard is one request from the
 * browser instead of six, which keeps the first paint fast.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int RECENT_LIMIT = 5;

    private final AttendanceQueryService attendanceQueryService;
    private final TimetableService timetableService;
    private final AssignmentService assignmentService;
    private final AnnouncementService announcementService;
    private final AcademicService academicService;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public StudentDashboardResponse studentDashboard() {
        Student student = currentUser.student();
        AttendanceSummaryResponse attendance = attendanceQueryService.mySummary();
        List<TimetableResponse> todayClasses = timetableService.myToday();

        List<AssignmentResponse> pending = assignmentService.listForCurrentUser().stream()
                .filter(a -> "PENDING".equals(a.submissionStatus()))
                .limit(RECENT_LIMIT)
                .toList();

        List<AnnouncementResponse> announcements = announcementService.recent(RECENT_LIMIT);
        var academics = academicService.mySummary();
        int graded = academics.semesters().stream().mapToInt(s -> s.subjectCount()).sum();

        return new StudentDashboardResponse(
                student.getUser().getFullName(),
                student.getRollNumber(),
                student.getDepartment().getName(),
                student.getSemester(),
                student.getDivision(),
                attendance,
                todayClasses,
                pending,
                announcements,
                academics.overallPercentage(),
                graded
        );
    }

    @Transactional(readOnly = true)
    public FacultyDashboardResponse facultyDashboard() {
        Faculty faculty = currentUser.faculty();

        int subjectsTaught = subjectRepository.findByFacultyId(faculty.getId()).size();
        long totalStudents = studentRepository.findByDepartmentId(faculty.getDepartment().getId()).size();
        long sessionsToday = sessionRepository.countByFacultyIdAndSessionDate(faculty.getId(), LocalDate.now());
        long pendingReviews = submissionRepository
                .countByAssignmentFacultyIdAndStatusNot(faculty.getId(), SubmissionStatus.GRADED);

        List<AttendanceSessionResponse> recentSessions = attendanceQueryService.sessionsForFaculty().stream()
                .limit(RECENT_LIMIT)
                .toList();

        return new FacultyDashboardResponse(
                faculty.getUser().getFullName(),
                faculty.getDepartment().getName(),
                faculty.getDesignation(),
                subjectsTaught,
                totalStudents,
                sessionsToday,
                pendingReviews,
                timetableService.myToday(),
                recentSessions,
                announcementService.recent(RECENT_LIMIT)
        );
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse adminDashboard() {
        List<Department> departments = departmentRepository.findAll();

        List<AdminDashboardResponse.DepartmentStat> stats = departments.stream()
                .map(department -> {
                    long total = recordRepository.countByStudentDepartmentId(department.getId());
                    long present = recordRepository
                            .countByStudentDepartmentIdAndStatus(department.getId(), AttendanceStatus.PRESENT);
                    return new AdminDashboardResponse.DepartmentStat(
                            department.getName(),
                            studentRepository.findByDepartmentId(department.getId()).size(),
                            facultyRepository.search(null, department.getId(),
                                    org.springframework.data.domain.Pageable.unpaged()).getTotalElements(),
                            GradeCalculator.percentage(present, total));
                })
                .toList();

        return new AdminDashboardResponse(
                studentRepository.count(),
                facultyRepository.count(),
                departmentRepository.count(),
                subjectRepository.count(),
                sessionRepository.count(),
                sessionRepository.countByStatus(SessionStatus.OPEN),
                attendanceQueryService.campusPercentage(),
                assignmentRepository.count(),
                submissionRepository.count(),
                stats,
                announcementService.recent(RECENT_LIMIT)
        );
    }
}
