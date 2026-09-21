package com.smartcampus.service;

import com.smartcampus.dto.common.NotificationResponse;
import com.smartcampus.entity.Notification;
import com.smartcampus.entity.User;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.mapper.NotificationMapper;
import com.smartcampus.repository.NotificationRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * In-app notifications. Written by other services when something happens that a
 * user should know about (new assignment, graded work, announcement).
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<NotificationResponse> myNotifications() {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.user().getId())
                .stream().map(NotificationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount() {
        return notificationRepository.countByUserIdAndReadFalse(currentUser.user().getId());
    }

    @Transactional
    public void markRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!notification.getUser().getId().equals(currentUser.user().getId())) {
            throw new UnauthorizedException("This notification belongs to someone else");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyUser(User user, String title, String message) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .read(false)
                .build());
    }

    /** Fan-out to one class. Saved in a single batch rather than one insert per student. */
    @Transactional
    public void notifyClass(Long departmentId, Integer semester, String division, String title, String message) {
        List<Notification> notifications = studentRepository
                .findByDepartmentIdAndSemesterAndDivision(departmentId, semester, division)
                .stream()
                .map(student -> Notification.builder()
                        .user(student.getUser())
                        .title(title)
                        .message(message)
                        .read(false)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void notifyDepartment(Long departmentId, String title, String message) {
        List<Notification> notifications = studentRepository.findByDepartmentId(departmentId).stream()
                .map(student -> Notification.builder()
                        .user(student.getUser())
                        .title(title)
                        .message(message)
                        .read(false)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
    }
}
