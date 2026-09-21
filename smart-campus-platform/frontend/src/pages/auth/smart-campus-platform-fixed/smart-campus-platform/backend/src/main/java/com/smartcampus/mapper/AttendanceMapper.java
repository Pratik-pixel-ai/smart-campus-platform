package com.smartcampus.mapper;

import com.smartcampus.dto.attendance.AttendanceRecordResponse;
import com.smartcampus.dto.attendance.AttendanceSessionResponse;
import com.smartcampus.entity.AttendanceRecord;
import com.smartcampus.entity.AttendanceSession;
import com.smartcampus.entity.AttendanceStatus;
import com.smartcampus.util.DateUtils;

import java.util.List;

public final class AttendanceMapper {

    private AttendanceMapper() {
    }

    public static AttendanceRecordResponse toResponse(AttendanceRecord record) {
        return new AttendanceRecordResponse(
                record.getId(),
                record.getStudent().getId(),
                record.getStudent().getUser().getFullName(),
                record.getStudent().getRollNumber(),
                record.getStatus().name(),
                record.getDetectionMethod().name(),
                record.getSignalStrength(),
                DateUtils.format(record.getMarkedAt()),
                record.getSession().getSubject().getName(),
                DateUtils.format(record.getSession().getSessionDate())
        );
    }

    public static AttendanceSessionResponse toResponse(AttendanceSession session,
                                                       List<AttendanceRecord> records,
                                                       int enrolledCount) {
        int present = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        int absent = (int) records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();

        return new AttendanceSessionResponse(
                session.getId(),
                session.getSubject().getId(),
                session.getSubject().getName(),
                session.getSubject().getCode(),
                session.getFaculty().getUser().getFullName(),
                session.getClassroom() == null ? "-" : session.getClassroom().getRoomNumber(),
                DateUtils.format(session.getSessionDate()),
                session.getLectureNumber(),
                session.getDurationMinutes(),
                session.getSemester(),
                session.getDivision(),
                session.getStatus().name(),
                session.getMode().name(),
                DateUtils.format(session.getStartedAt()),
                DateUtils.format(session.getClosedAt()),
                enrolledCount,
                present,
                absent,
                records.stream().map(AttendanceMapper::toResponse).toList()
        );
    }
}
