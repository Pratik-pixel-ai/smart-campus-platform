package com.smartcampus.service.impl;

import com.smartcampus.dto.attendance.DetectionRequest;
import com.smartcampus.entity.AttendanceSession;
import com.smartcampus.entity.DetectionMethod;
import com.smartcampus.entity.Student;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import com.smartcampus.security.CurrentUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Simulated detection for demos and for machines without BLE hardware.
 *
 * The faculty screen sends the student id it wants to mark present; everything after
 * that (enrolment check, duplicate prevention, persistence) is identical to real BLE.
 * Records created here are stored with detection_method = DEMO so nobody can mistake
 * simulated attendance for a hardware-verified scan.
 */
@Service
@ConditionalOnProperty(name = "app.attendance.mode", havingValue = "demo", matchIfMissing = true)
public class DemoAttendanceService extends AbstractAttendanceService {

    public DemoAttendanceService(AttendanceSessionRepository sessionRepository,
                                 AttendanceRecordRepository recordRepository,
                                 StudentRepository studentRepository,
                                 SubjectRepository subjectRepository,
                                 ClassroomRepository classroomRepository,
                                 CurrentUser currentUser) {
        super(sessionRepository, recordRepository, studentRepository, subjectRepository, classroomRepository, currentUser);
    }

    @Override
    protected Student resolveStudent(AttendanceSession session, DetectionRequest request) {
        if (request.studentId() == null) {
            throw new BadRequestException("Select a student to simulate a detection");
        }
        return studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.studentId()));
    }

    @Override
    public DetectionMethod mode() {
        return DetectionMethod.DEMO;
    }
}
