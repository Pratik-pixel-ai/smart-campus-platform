package com.smartcampus.dto.people;

public record FacultyResponse(
        Long id,
        Long userId,
        String fullName,
        String email,
        String employeeCode,
        Long departmentId,
        String departmentName,
        String designation,
        String phone,
        boolean active
) {
}
