package com.smartcampus.dto.master;

public record DepartmentResponse(Long id, String name, String code, String hodName,
                                 long studentCount, long facultyCount) {
}
