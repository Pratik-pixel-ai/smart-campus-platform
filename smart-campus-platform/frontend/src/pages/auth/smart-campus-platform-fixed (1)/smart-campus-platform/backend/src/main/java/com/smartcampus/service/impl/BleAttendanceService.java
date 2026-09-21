package com.smartcampus.service.impl;

import com.smartcampus.dto.attendance.DetectionRequest;
import com.smartcampus.entity.AttendanceSession;
import com.smartcampus.entity.DetectionMethod;
import com.smartcampus.entity.Student;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import com.smartcampus.security.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Real BLE detection. The faculty client (Web Bluetooth in the browser, or the
 * Android scanner described in the docs) scans nearby advertisements and posts each
 * one here as {bleDeviceId, rssi}.
 *
 * This service does two extra things the demo service does not:
 *   1. maps the advertised device id back to a student (indexed unique column lookup);
 *   2. rejects weak signals, so a phone in the corridor is not counted as present.
 *
 * Activate with app.attendance.mode=ble (ATTENDANCE_MODE=ble).
 */
@Service
@ConditionalOnProperty(name = "app.attendance.mode", havingValue = "ble")
public class BleAttendanceService extends AbstractAttendanceService {

    private final int minRssi;

    public BleAttendanceService(AttendanceSessionRepository sessionRepository,
                                AttendanceRecordRepository recordRepository,
                                StudentRepository studentRepository,
                                SubjectRepository subjectRepository,
                                ClassroomRepository classroomRepository,
                                CurrentUser currentUser,
                                @Value("${app.attendance.min-rssi:-85}") int minRssi) {
        super(sessionRepository, recordRepository, studentRepository, subjectRepository, classroomRepository, currentUser);
        this.minRssi = minRssi;
    }

    @Override
    protected Student resolveStudent(AttendanceSession session, DetectionRequest request) {
        if (request.bleDeviceId() == null || request.bleDeviceId().isBlank()) {
            throw new BadRequestException("A BLE device identifier is required in BLE mode");
        }
        if (request.rssi() != null && request.rssi() < minRssi) {
            throw new BadRequestException("Signal too weak to confirm the student is in the classroom");
        }
        return studentRepository.findByBleDeviceId(request.bleDeviceId().trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No student is registered for BLE device " + request.bleDeviceId()));
    }

    @Override
    public DetectionMethod mode() {
        return DetectionMethod.BLE;
    }
}
