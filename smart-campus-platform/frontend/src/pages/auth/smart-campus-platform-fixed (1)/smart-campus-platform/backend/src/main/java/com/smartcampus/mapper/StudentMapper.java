package com.smartcampus.mapper;

import com.smartcampus.dto.people.StudentResponse;
import com.smartcampus.entity.Student;

/**
 * Entity -> DTO conversion. Keeping this out of the services stops password and
 * other internal fields from leaking into API responses by accident.
 */
public final class StudentMapper {

    private StudentMapper() {
    }

    public static StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getUser().getId(),
                student.getUser().getFullName(),
                student.getUser().getEmail(),
                student.getRollNumber(),
                student.getDepartment().getId(),
                student.getDepartment().getName(),
                student.getSemester(),
                student.getDivision(),
                student.getPhone(),
                student.getBleDeviceId(),
                student.getUser().isActive()
        );
    }
}
