package com.smartcampus.controller;

import com.smartcampus.dto.dashboard.AdminDashboardResponse;
import com.smartcampus.dto.dashboard.FacultyDashboardResponse;
import com.smartcampus.dto.dashboard.StudentDashboardResponse;
import com.smartcampus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/student")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<StudentDashboardResponse> student() {
        return ResponseEntity.ok(dashboardService.studentDashboard());
    }

    @GetMapping("/faculty")
    @PreAuthorize("hasAuthority('ROLE_FACULTY')")
    public ResponseEntity<FacultyDashboardResponse> faculty() {
        return ResponseEntity.ok(dashboardService.facultyDashboard());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AdminDashboardResponse> admin() {
        return ResponseEntity.ok(dashboardService.adminDashboard());
    }
}
