package com.smartcampus.controller;

import com.smartcampus.dto.common.MessageResponse;
import com.smartcampus.dto.common.PageResponse;
import com.smartcampus.dto.people.StudentCreateRequest;
import com.smartcampus.dto.people.StudentResponse;
import com.smartcampus.dto.people.StudentUpdateRequest;
import com.smartcampus.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /** GET /api/students?page=0&size=10&sort=rollNumber,asc&search=&departmentId=&semester= */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_FACULTY')")
    public ResponseEntity<PageResponse<StudentResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer semester,
            @PageableDefault(size = 10, sort = "rollNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(studentService.list(search, departmentId, semester, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_STUDENT')")
    public ResponseEntity<StudentResponse> me() {
        return ResponseEntity.ok(studentService.me());
    }

    /** Class list used by the attendance screen. */
    @GetMapping("/class")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_FACULTY')")
    public ResponseEntity<List<StudentResponse>> classList(@RequestParam Long departmentId,
                                                           @RequestParam Integer semester,
                                                           @RequestParam String division) {
        return ResponseEntity.ok(studentService.classList(departmentId, semester, division));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_FACULTY')")
    public ResponseEntity<StudentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<StudentResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody StudentUpdateRequest request) {
        return ResponseEntity.ok(studentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Student removed"));
    }
}
