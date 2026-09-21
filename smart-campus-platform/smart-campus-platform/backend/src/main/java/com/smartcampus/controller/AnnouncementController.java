package com.smartcampus.controller;

import com.smartcampus.dto.announcement.AnnouncementRequest;
import com.smartcampus.dto.announcement.AnnouncementResponse;
import com.smartcampus.dto.common.MessageResponse;
import com.smartcampus.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> list() {
        return ResponseEntity.ok(announcementService.listForCurrentUser());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<AnnouncementResponse> create(@Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.create(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_FACULTY','ROLE_ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Announcement removed"));
    }
}
