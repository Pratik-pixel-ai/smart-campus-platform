package com.smartcampus.dto.common;

public record NotificationResponse(Long id, String title, String message, boolean read, String createdAt) {
}
