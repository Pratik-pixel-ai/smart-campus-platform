package com.smartcampus.service;

import com.smartcampus.dto.attendance.*;
import com.smartcampus.entity.*;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.mapper.AttendanceMapper;
import com.smartcampus.repository.AttendanceRecordRepository;
import com.smartcampus.repository.AttendanceSessionRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read side of attendance: history, percentages and reports.
 * All percentages are computed here, never in the browser, so a student cannot
 * change what their attendance looks like by editing the page.
 */
@Service
@RequiredArgsConstructor
public class AttendanceQueryService {

    /** College rule used across the platform. */
    public static final double MINIMUM_ATTENDANCE_PERCENTAGE = 75.0;

    private final AttendanceRecordRepository recordRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse summaryForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        ensureCanView(student);
        return buildSummary(student);
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse mySummary() {
        return buildSummary(currentUser.student());
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> historyForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        ensureCanView(student);
        return recordRepository.findByStudentIdOrderByMarkedAtDesc(studentId).stream()
                .map(AttendanceMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> sessionsForFaculty() {
        Faculty faculty = currentUser.faculty();
        return sessionRepository.findByFacultyIdOrderByStartedAtDesc(faculty.getId()).stream()
                .map(this::toLightResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionResponse> allSessions(Pageable pageable) {
        return sessionRepository.findAllByOrderByStartedAtDesc(pageable).getContent().stream()
                .map(this::toLightResponse)
                .toList();
    }

    /**
     * Campus-wide attendance percentage. One aggregate query pair instead of walking
     * every record in Java.
     */
    @Transactional(readOnly = true)
    public double campusPercentage() {
        long total = recordRepository.count();
        long present = recordRepository.countByStatus(AttendanceStatus.PRESENT);
        return GradeCalculator.percentage(present, total);
    }

    private AttendanceSummaryResponse buildSummary(Student student) {
        List<SubjectAttendanceProjection> rows = recordRepository.findSubjectWiseAttendance(student.getId());

        List<SubjectAttendanceResponse> subjects = rows.stream()
                .map(row -> new SubjectAttendanceResponse(
                        row.getSubjectId(),
                        row.getSubjectName(),
                        row.getSubjectCode(),
                        row.getTotalLectures(),
                        row.getPresentLectures(),
                        row.getTotalLectures() - row.getPresentLectures(),
                        GradeCalculator.percentage(row.getPresentLectures(), row.getTotalLectures())))
                .toList();

        long total = subjects.stream().mapToLong(SubjectAttendanceResponse::totalLectures).sum();
        long present = subjects.stream().mapToLong(SubjectAttendanceResponse::presentLectures).sum();

        return new AttendanceSummaryResponse(
                student.getId(),
                student.getUser().getFullName(),
                student.getRollNumber(),
                total,
                present,
                total - present,
                GradeCalculator.percentage(present, total),
                lecturesCanMiss(present, total),
                subjects
        );
    }

    /**
     * How many further lectures can be missed while staying at or above 75%.
     * present / (total + x) >= 0.75  ->  x <= present/0.75 - total
     */
    private long lecturesCanMiss(long present, long total) {
        if (total == 0) {
            return 0;
        }
        double allowed = (present / (MINIMUM_ATTENDANCE_PERCENTAGE / 100.0)) - total;
        return Math.max(0, (long) Math.floor(allowed));
    }

    private AttendanceSessionResponse toLightResponse(AttendanceSession session) {
        List<AttendanceRecord> records = recordRepository.findBySessionIdOrderByMarkedAtAsc(session.getId());
        int enrolled = studentRepository.findByDepartmentIdAndSemesterAndDivision(
                session.getSubject().getDepartment().getId(),
                session.getSemester(),
                session.getDivision()).size();
        return AttendanceMapper.toResponse(session, records, enrolled);
    }

    /** A student may only read their own attendance; faculty and admin may read anyone's. */
    private void ensureCanView(Student student) {
        User user = currentUser.user();
        if (user.getRole() == Role.ROLE_STUDENT && !student.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only view your own attendance");
        }
    }
}
