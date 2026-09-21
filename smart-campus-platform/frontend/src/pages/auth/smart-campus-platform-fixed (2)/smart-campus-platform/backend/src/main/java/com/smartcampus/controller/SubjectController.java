package com.smartcampus.controller;

import com.smartcampus.dto.common.MessageResponse;
import com.smartcampus.dto.common.PageResponse;
import com.smartcampus.dto.master.SubjectRequest;
import com.smartcampus.dto.master.SubjectResponse;
import com.smartcampus.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping
    public ResponseEntity<PageResponse<SubjectResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 10, sort = "code") Pageable pageable) {
        return ResponseEntity.ok(subjectService.list(search, departmentId, pageable));
    }

    @GetMapping("/options")
    public ResponseEntity<List<SubjectResponse>> options() {
        return ResponseEntity.ok(subjectService.listAll());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('ROLE_FACULTY')")
    public ResponseEntity<List<SubjectResponse>> mine(@RequestParam Long facultyId) {
        return ResponseEntity.ok(subjectService.listForFaculty(facultyId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SubjectResponse> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(subjectService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Subject removed"));
    }
}
