package com.smartcampus.service;

import com.smartcampus.dto.announcement.AnnouncementRequest;
import com.smartcampus.dto.announcement.AnnouncementResponse;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.mapper.AnnouncementMapper;
import com.smartcampus.repository.AnnouncementRepository;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    /** Students and faculty see campus-wide notices plus their own department's. */
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listForCurrentUser() {
        User user = currentUser.user();
        Long departmentId = switch (user.getRole()) {
            case ROLE_STUDENT -> currentUser.student().getDepartment().getId();
            case ROLE_FACULTY -> currentUser.faculty().getDepartment().getId();
            case ROLE_ADMIN -> null;
        };

        List<Announcement> announcements = departmentId == null
                ? announcementRepository.findAllByOrderByCreatedAtDesc()
                : announcementRepository.findVisibleForDepartment(departmentId);

        return announcements.stream().map(AnnouncementMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> recent(int limit) {
        return listForCurrentUser().stream().limit(limit).toList();
    }

    @Transactional
    public AnnouncementResponse create(AnnouncementRequest request) {
        User author = currentUser.user();

        Department department = request.departmentId() == null ? null
                : departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));

        Subject subject = request.subjectId() == null ? null
                : subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));

        Announcement announcement = announcementRepository.save(Announcement.builder()
                .title(request.title())
                .message(request.message())
                .department(department)
                .subject(subject)
                .priority(parsePriority(request.priority()))
                .createdBy(author)
                .build());

        if (department != null) {
            notificationService.notifyDepartment(department.getId(), announcement.getTitle(), announcement.getMessage());
        }

        return AnnouncementMapper.toResponse(announcement);
    }

    @Transactional
    public void delete(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", id));

        if (!currentUser.isAdmin() && !announcement.getCreatedBy().getId().equals(currentUser.user().getId())) {
            throw new UnauthorizedException("This announcement was posted by someone else");
        }
        announcementRepository.delete(announcement);
    }

    private Priority parsePriority(String value) {
        try {
            return Priority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Priority must be LOW, NORMAL or HIGH");
        }
    }
}
