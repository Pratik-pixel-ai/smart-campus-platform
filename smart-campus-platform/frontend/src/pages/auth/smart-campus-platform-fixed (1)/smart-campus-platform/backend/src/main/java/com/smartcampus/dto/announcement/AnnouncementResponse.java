package com.smartcampus.dto.announcement;

public record AnnouncementResponse(
        Long id,
        String title,
        String message,
        Long departmentId,
        String departmentName,
        Long subjectId,
        String subjectName,
        String priority,
        String createdByName,
        String createdByRole,
        String createdAt
) {
}
