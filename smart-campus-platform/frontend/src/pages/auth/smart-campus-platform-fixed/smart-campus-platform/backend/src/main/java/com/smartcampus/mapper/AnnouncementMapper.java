package com.smartcampus.mapper;

import com.smartcampus.dto.announcement.AnnouncementResponse;
import com.smartcampus.entity.Announcement;
import com.smartcampus.util.DateUtils;

public final class AnnouncementMapper {

    private AnnouncementMapper() {
    }

    public static AnnouncementResponse toResponse(Announcement announcement) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getMessage(),
                announcement.getDepartment() == null ? null : announcement.getDepartment().getId(),
                announcement.getDepartment() == null ? "All departments" : announcement.getDepartment().getName(),
                announcement.getSubject() == null ? null : announcement.getSubject().getId(),
                announcement.getSubject() == null ? null : announcement.getSubject().getName(),
                announcement.getPriority().name(),
                announcement.getCreatedBy().getFullName(),
                announcement.getCreatedBy().getRole().name(),
                DateUtils.format(announcement.getCreatedAt())
        );
    }
}
