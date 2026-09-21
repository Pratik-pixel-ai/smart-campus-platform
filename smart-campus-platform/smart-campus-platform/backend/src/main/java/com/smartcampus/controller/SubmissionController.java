package com.smartcampus.controller;

import com.smartcampus.dto.assignment.GradeRequest;
import com.smartcampus.dto.assignment.SubmissionRequest;
import com.smartcampus.dto.assignment.SubmissionResponse;
import com.smartcampus.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<SubmissionResponse> submit(@Valid @RequestBody SubmissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionService.submit(request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<List<SubmissionResponse>> mySubmissions() {
        return ResponseEntity.ok(submissionService.mySubmissions());
    }

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<List<SubmissionResponse>> forAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(submissionService.forAssignment(assignmentId));
    }

    @PutMapping("/{id}/grade")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<SubmissionResponse> grade(@PathVariable Long id, @Valid @RequestBody GradeRequest request) {
        return ResponseEntity.ok(submissionService.grade(id, request));
    }
}
