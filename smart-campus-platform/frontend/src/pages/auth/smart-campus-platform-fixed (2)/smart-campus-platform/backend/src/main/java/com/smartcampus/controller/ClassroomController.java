package com.smartcampus.controller;

import com.smartcampus.dto.common.MessageResponse;
import com.smartcampus.dto.common.PageResponse;
import com.smartcampus.dto.master.ClassroomRequest;
import com.smartcampus.dto.master.ClassroomResponse;
import com.smartcampus.service.ClassroomService;
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
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @GetMapping
    public ResponseEntity<PageResponse<ClassroomResponse>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "roomNumber") Pageable pageable) {
        return ResponseEntity.ok(classroomService.list(search, pageable));
    }

    @GetMapping("/options")
    public ResponseEntity<List<ClassroomResponse>> options() {
        return ResponseEntity.ok(classroomService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ClassroomResponse> create(@Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classroomService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ClassroomResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.ok(classroomService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        classroomService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Classroom removed"));
    }
}
