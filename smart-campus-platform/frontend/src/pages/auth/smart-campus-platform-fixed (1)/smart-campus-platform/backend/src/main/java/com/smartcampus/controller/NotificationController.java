package com.smartcampus.controller;

import com.smartcampus.dto.common.MessageResponse;
import com.smartcampus.dto.common.NotificationResponse;
import com.smartcampus.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> myNotifications() {
        return ResponseEntity.ok(notificationService.myNotifications());
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }
}
