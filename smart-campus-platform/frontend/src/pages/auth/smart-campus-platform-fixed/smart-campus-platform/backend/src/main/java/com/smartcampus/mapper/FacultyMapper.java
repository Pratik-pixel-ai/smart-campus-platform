package com.smartcampus.mapper;

import com.smartcampus.dto.people.FacultyResponse;
import com.smartcampus.entity.Faculty;

public final class FacultyMapper {

    private FacultyMapper() {
    }

    public static FacultyResponse toResponse(Faculty faculty) {
        return new FacultyResponse(
                faculty.getId(),
                faculty.getUser().getId(),
                faculty.getUser().getFullName(),
                faculty.getUser().getEmail(),
                faculty.getEmployeeCode(),
                faculty.getDepartment().getId(),
                faculty.getDepartment().getName(),
                faculty.getDesignation(),
                faculty.getPhone(),
                faculty.getUser().isActive()
        );
    }
}
