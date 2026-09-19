package com.smartcampus.dto.master;

public record SubjectResponse(Long id, String name, String code, Long departmentId, String departmentName,
                              Long facultyId, String facultyName, Integer semester, Integer credits) {
}
