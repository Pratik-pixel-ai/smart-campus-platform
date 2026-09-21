package com.smartcampus.controller;

import com.smartcampus.dto.assignment.AssignmentRequest;
import com.smartcampus.dto.assignment.AssignmentResponse;
import com.smartcampus.dto.common.MessageResponse;
import com.smartcampus.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> list() {
        return ResponseEntity.ok(assignmentService.listForCurrentUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<AssignmentResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody AssignmentRequest request) {
        return ResponseEntity.ok(assignmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        assignmentService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Assignment removed"));
    }
}
