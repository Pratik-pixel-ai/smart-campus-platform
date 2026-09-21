package com.smartcampus.mapper;

import com.smartcampus.dto.timetable.TimetableResponse;
import com.smartcampus.entity.TimetableEntry;
import com.smartcampus.util.DateUtils;

public final class TimetableMapper {

    private TimetableMapper() {
    }

    public static TimetableResponse toResponse(TimetableEntry entry) {
        return new TimetableResponse(
                entry.getId(),
                entry.getDayOfWeek().name(),
                DateUtils.formatTime(entry.getStartTime()),
                DateUtils.formatTime(entry.getEndTime()),
                entry.getSubject().getId(),
                entry.getSubject().getName(),
                entry.getSubject().getCode(),
                entry.getFaculty().getId(),
                entry.getFaculty().getUser().getFullName(),
                entry.getClassroom() == null ? null : entry.getClassroom().getId(),
                entry.getClassroom() == null ? "-" : entry.getClassroom().getRoomNumber(),
                entry.getSemester(),
                entry.getDivision(),
                entry.getSubject().getDepartment().getName()
        );
    }
}
