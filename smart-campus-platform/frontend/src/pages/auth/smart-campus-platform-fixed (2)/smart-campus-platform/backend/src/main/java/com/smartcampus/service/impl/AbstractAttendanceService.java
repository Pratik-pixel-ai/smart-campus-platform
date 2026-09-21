package com.smartcampus.service.impl;

import com.smartcampus.dto.attendance.AttendanceSessionRequest;
import com.smartcampus.dto.attendance.AttendanceSessionResponse;
import com.smartcampus.dto.attendance.DetectionRequest;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.mapper.AttendanceMapper;
import com.smartcampus.repository.*;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.service.AttendanceService;
import com.smartcampus.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Everything the two attendance strategies share: session lifecycle, duplicate
 * prevention and persistence. Only the step that turns a detection event into a
 * student differs, and that is left abstract.
 */
@RequiredArgsConstructor
public abstract class AbstractAttendanceService implements AttendanceService {

    protected final AttendanceSessionRepository sessionRepository;
    protected final AttendanceRecordRepository recordRepository;
    protected final StudentRepository studentRepository;
    protected final SubjectRepository subjectRepository;
    protected final ClassroomRepository classroomRepository;
    protected final CurrentUser currentUser;

    /**
     * Turns one detection event into the student it belongs to.
     * Demo mode trusts a student id from the UI; BLE mode resolves a scanned device id
     * and checks signal strength.
     */
    protected abstract Student resolveStudent(AttendanceSession session, DetectionRequest request);

    @Override
    @Transactional
    public AttendanceSessionResponse createSession(AttendanceSessionRequest request) {
        Faculty faculty = currentUser.faculty();
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));

        AttendanceSession session = AttendanceSession.builder()
                .subject(subject)
                .faculty(faculty)
                .classroom(request.classroomId() == null ? null : classroomRepository.findById(request.classroomId())
                        .orElseThrow(() -> new ResourceNotFoundException("Classroom", request.classroomId())))
                .sessionDate(DateUtils.parseDateOrToday(request.sessionDate()))
                .lectureNumber(request.lectureNumber())
                .durationMinutes(request.durationMinutes())
                .semester(request.semester())
                .division(request.division().toUpperCase())
                .status(SessionStatus.OPEN)
                .mode(mode())
                .startedAt(LocalDateTime.now())
                .build();

        return toResponse(sessionRepository.save(session));
    }

    @Override
    @Transactional
    public AttendanceSessionResponse detect(Long sessionId, DetectionRequest request) {
        AttendanceSession session = findOwnedSession(sessionId);
        if (session.getStatus() == SessionStatus.CLOSED) {
            throw new BadRequestException("This session is closed. Start a new session to mark attendance.");
        }

        Student student = resolveStudent(session, request);
        ensureEnrolled(session, student);

        // Duplicate prevention: one record per (session, student). The unique constraint
        // on the table is the real guarantee; this check turns it into a friendly no-op.
        if (recordRepository.existsBySessionIdAndStudentId(session.getId(), student.getId())) {
            return toResponse(session);
        }

        recordRepository.save(AttendanceRecord.builder()
                .session(session)
                .student(student)
                .status(AttendanceStatus.PRESENT)
                .detectionMethod(mode())
                .signalStrength(request.rssi())
                .markedAt(LocalDateTime.now())
                .build());

        return toResponse(session);
    }

    @Override
    @Transactional
    public AttendanceSessionResponse closeSession(Long sessionId) {
        AttendanceSession session = findOwnedSession(sessionId);
        if (session.getStatus() == SessionStatus.CLOSED) {
            return toResponse(session);
        }

        // Build a set of the students already marked present, then write ABSENT for the rest.
        // Map lookup keeps this O(n) over the class list instead of a query per student.
        Map<Long, AttendanceRecord> existing = recordRepository.findBySessionIdOrderByMarkedAtAsc(session.getId())
                .stream()
                .collect(Collectors.toMap(record -> record.getStudent().getId(), Function.identity()));

        List<AttendanceRecord> absentees = enrolledStudents(session).stream()
                .filter(student -> !existing.containsKey(student.getId()))
                .map(student -> AttendanceRecord.builder()
                        .session(session)
                        .student(student)
                        .status(AttendanceStatus.ABSENT)
                        .detectionMethod(DetectionMethod.SYSTEM)
                        .markedAt(LocalDateTime.now())
                        .build())
                .toList();

        recordRepository.saveAll(absentees);

        session.setStatus(SessionStatus.CLOSED);
        session.setClosedAt(LocalDateTime.now());
        sessionRepository.save(session);

        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public AttendanceSessionResponse getSession(Long sessionId) {
        return toResponse(sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session", sessionId)));
    }

    /** The class list for a session: same department, semester and division as the subject. */
    protected List<Student> enrolledStudents(AttendanceSession session) {
        return studentRepository.findByDepartmentIdAndSemesterAndDivision(
                session.getSubject().getDepartment().getId(),
                session.getSemester(),
                session.getDivision());
    }

    protected void ensureEnrolled(AttendanceSession session, Student student) {
        boolean sameClass = student.getDepartment().getId().equals(session.getSubject().getDepartment().getId())
                && student.getSemester().equals(session.getSemester())
                && student.getDivision().equalsIgnoreCase(session.getDivision());
        if (!sameClass) {
            throw new BadRequestException(student.getUser().getFullName() + " is not part of this class");
        }
    }

    /** A faculty member can only touch their own sessions; admins can touch any. */
    protected AttendanceSession findOwnedSession(Long sessionId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session", sessionId));

        if (currentUser.isAdmin()) {
            return session;
        }
        Faculty faculty = currentUser.faculty();
        if (!session.getFaculty().getId().equals(faculty.getId())) {
            throw new UnauthorizedException("This attendance session belongs to another faculty member");
        }
        return session;
    }

    protected AttendanceSessionResponse toResponse(AttendanceSession session) {
        List<AttendanceRecord> records = recordRepository.findBySessionIdOrderByMarkedAtAsc(session.getId());
        return AttendanceMapper.toResponse(session, records, enrolledStudents(session).size());
    }

    protected LocalDate today() {
        return LocalDate.now();
    }
}
