package com.smartcampus.mapper;

import com.smartcampus.dto.common.NotificationResponse;
import com.smartcampus.entity.Notification;
import com.smartcampus.util.DateUtils;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                DateUtils.format(notification.getCreatedAt())
        );
    }
}
