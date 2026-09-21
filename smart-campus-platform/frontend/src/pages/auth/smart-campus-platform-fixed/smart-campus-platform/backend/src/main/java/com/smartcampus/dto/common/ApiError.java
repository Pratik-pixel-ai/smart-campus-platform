package com.smartcampus.dto.common;

import java.time.LocalDateTime;
import java.util.Map;

/** Single error shape returned by GlobalExceptionHandler for every failure. */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
