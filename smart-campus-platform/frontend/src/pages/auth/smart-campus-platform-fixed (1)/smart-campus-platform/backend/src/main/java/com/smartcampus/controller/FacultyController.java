package com.smartcampus.controller;

import com.smartcampus.dto.common.MessageResponse;
import com.smartcampus.dto.common.PageResponse;
import com.smartcampus.dto.people.FacultyCreateRequest;
import com.smartcampus.dto.people.FacultyResponse;
import com.smartcampus.dto.people.FacultyUpdateRequest;
import com.smartcampus.service.FacultyService;
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
@RequestMapping("/api/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PageResponse<FacultyResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @PageableDefault(size = 10, sort = "employeeCode") Pageable pageable) {
        return ResponseEntity.ok(facultyService.list(search, departmentId, pageable));
    }

    /** Flat list for dropdowns (assigning a subject or a timetable slot). */
    @GetMapping("/options")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_FACULTY')")
    public ResponseEntity<List<FacultyResponse>> options() {
        return ResponseEntity.ok(facultyService.listAll());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_FACULTY')")
    public ResponseEntity<FacultyResponse> me() {
        return ResponseEntity.ok(facultyService.me());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FacultyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facultyService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FacultyResponse> create(@Valid @RequestBody FacultyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facultyService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<FacultyResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody FacultyUpdateRequest request) {
        return ResponseEntity.ok(facultyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        facultyService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Faculty member removed"));
    }
}
