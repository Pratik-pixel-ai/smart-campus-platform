package com.smartcampus.util;

import com.smartcampus.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Small helpers so controllers and mappers do not repeat null checks and parsing. */
public final class DateUtils {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private DateUtils() {
    }

    public static String format(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    public static String format(LocalDate value) {
        return value == null ? null : value.toString();
    }

    public static String formatTime(java.time.LocalTime value) {
        return value == null ? null : value.format(TIME);
    }

    public static LocalDate parseDateOrToday(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid date: use the format YYYY-MM-DD");
        }
    }

    /** Accepts both "2026-03-01T23:59" and "2026-03-01T23:59:00". */
    public static LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid date and time: use the format YYYY-MM-DDTHH:mm");
        }
    }

    public static java.time.LocalTime parseTime(String value) {
        try {
            return java.time.LocalTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Invalid time: use the format HH:mm");
        }
    }
}
