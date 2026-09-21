package com.smartcampus.service;

import com.smartcampus.dto.attendance.AttendanceSessionRequest;
import com.smartcampus.dto.attendance.AttendanceSessionResponse;
import com.smartcampus.dto.attendance.DetectionRequest;
import com.smartcampus.entity.DetectionMethod;

/**
 * Attendance capture, expressed as an interface so the way students are detected can
 * change without touching controllers or the rest of the application.
 *
 * Implementations:
 *   DemoAttendanceService - simulated detection, used when no BLE hardware is present.
 *   BleAttendanceService  - real BLE advertisements forwarded by the faculty scanner.
 *
 * Which one is active is decided by the app.attendance.mode property.
 */
public interface AttendanceService {

    AttendanceSessionResponse createSession(AttendanceSessionRequest request);

    /** Records one detection event and marks that student present. */
    AttendanceSessionResponse detect(Long sessionId, DetectionRequest request);

    /** Closes the session and writes ABSENT for every student who was never detected. */
    AttendanceSessionResponse closeSession(Long sessionId);

    /** Full session detail including every attendance record. */
    AttendanceSessionResponse getSession(Long sessionId);

    /** DEMO or BLE - stored on the session so the record is honest about its source. */
    DetectionMethod mode();
}
