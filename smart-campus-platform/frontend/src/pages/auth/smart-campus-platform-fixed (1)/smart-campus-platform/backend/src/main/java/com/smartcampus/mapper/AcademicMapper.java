package com.smartcampus.mapper;

import com.smartcampus.dto.academic.AcademicRecordResponse;
import com.smartcampus.entity.AcademicRecord;

public final class AcademicMapper {

    private AcademicMapper() {
    }

    public static AcademicRecordResponse toResponse(AcademicRecord record) {
        return new AcademicRecordResponse(
                record.getId(),
                record.getStudent().getId(),
                record.getStudent().getUser().getFullName(),
                record.getStudent().getRollNumber(),
                record.getSubject().getId(),
                record.getSubject().getName(),
                record.getSubject().getCode(),
                record.getSemester(),
                record.getInternalMarks(),
                record.getExternalMarks(),
                record.getTotalMarks(),
                record.getGrade()
        );
    }
}
