package com.smartcampus.controller;

import com.smartcampus.dto.common.MessageResponse;
import com.smartcampus.dto.timetable.TimetableRequest;
import com.smartcampus.dto.timetable.TimetableResponse;
import com.smartcampus.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    /** The signed-in user's week: their class if a student, their lectures if faculty. */
    @GetMapping("/me")
    public ResponseEntity<List<TimetableResponse>> myWeek() {
        return ResponseEntity.ok(timetableService.myWeek());
    }

    @GetMapping("/today")
    public ResponseEntity<List<TimetableResponse>> today() {
        return ResponseEntity.ok(timetableService.myToday());
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_FACULTY')")
    public ResponseEntity<List<TimetableResponse>> list() {
        return ResponseEntity.ok(timetableService.listAll());
    }

    @GetMapping("/class")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_FACULTY')")
    public ResponseEntity<List<TimetableResponse>> forClass(@RequestParam Long departmentId,
                                                            @RequestParam Integer semester,
                                                            @RequestParam String division) {
        return ResponseEntity.ok(timetableService.forClass(departmentId, semester, division));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TimetableResponse> create(@Valid @RequestBody TimetableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timetableService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TimetableResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody TimetableRequest request) {
        return ResponseEntity.ok(timetableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        timetableService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Timetable entry removed"));
    }
}
