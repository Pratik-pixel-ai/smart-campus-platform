package com.smartcampus.controller;

import com.smartcampus.dto.attendance.*;
import com.smartcampus.service.AttendanceQueryService;
import com.smartcampus.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Attendance endpoints.
 * Writing attendance is faculty/admin only; students can read their own figures.
 * The controller talks to the AttendanceService interface, so switching from demo
 * detection to real BLE needs no change here.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceQueryService attendanceQueryService;

    @PostMapping("/sessions")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<AttendanceSessionResponse> createSession(
            @Valid @RequestBody AttendanceSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.createSession(request));
    }

    /** One detection event: a simulated tap in demo mode, a BLE advertisement in BLE mode. */
    @PostMapping("/sessions/{id}/detect")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<AttendanceSessionResponse> detect(@PathVariable Long id,
                                                            @RequestBody DetectionRequest request) {
        return ResponseEntity.ok(attendanceService.detect(id, request));
    }

    @PostMapping("/sessions/{id}/close")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<AttendanceSessionResponse> close(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.closeSession(id));
    }

    @GetMapping("/sessions/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<AttendanceSessionResponse> session(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getSession(id));
    }

    @GetMapping("/sessions")
    @PreAuthorize("hasAuthority('ROLE_FACULTY')")
    public ResponseEntity<List<AttendanceSessionResponse>> mySessions() {
        return ResponseEntity.ok(attendanceQueryService.sessionsForFaculty());
    }

    @GetMapping("/sessions/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<AttendanceSessionResponse>> allSessions(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(attendanceQueryService.allSessions(pageable));
    }

    /** Which detection strategy is active - the UI labels demo attendance clearly. */
    @GetMapping("/mode")
    public ResponseEntity<Map<String, String>> mode() {
        return ResponseEntity.ok(Map.of("mode", attendanceService.mode().name()));
    }

    @GetMapping("/me/summary")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<AttendanceSummaryResponse> mySummary() {
        return ResponseEntity.ok(attendanceQueryService.mySummary());
    }

    @GetMapping("/student/{studentId}/summary")
    public ResponseEntity<AttendanceSummaryResponse> studentSummary(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceQueryService.summaryForStudent(studentId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceRecordResponse>> studentHistory(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceQueryService.historyForStudent(studentId));
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_FACULTY')")
    public ResponseEntity<Map<String, Object>> overview() {
        return ResponseEntity.ok(Map.of("campusPercentage", attendanceQueryService.campusPercentage()));
    }
}
