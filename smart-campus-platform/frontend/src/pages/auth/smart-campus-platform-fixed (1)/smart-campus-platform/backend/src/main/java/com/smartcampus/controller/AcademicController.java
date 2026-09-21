package com.smartcampus.controller;

import com.smartcampus.dto.academic.AcademicRecordRequest;
import com.smartcampus.dto.academic.AcademicRecordResponse;
import com.smartcampus.dto.academic.AcademicSummaryResponse;
import com.smartcampus.dto.common.MessageResponse;
import com.smartcampus.service.AcademicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academics")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicService academicService;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<AcademicSummaryResponse> mySummary() {
        return ResponseEntity.ok(academicService.mySummary());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<AcademicSummaryResponse> studentSummary(@PathVariable Long studentId) {
        return ResponseEntity.ok(academicService.summaryForStudent(studentId));
    }

    /** Create or update the marks for one student, subject and semester. */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<AcademicRecordResponse> save(@Valid @RequestBody AcademicRecordRequest request) {
        return ResponseEntity.ok(academicService.save(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        academicService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Academic record removed"));
    }
}
