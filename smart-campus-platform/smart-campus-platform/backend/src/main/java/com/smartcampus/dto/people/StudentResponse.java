package com.smartcampus.dto.people;

public record StudentResponse(
        Long id,
        Long userId,
        String fullName,
        String email,
        String rollNumber,
        Long departmentId,
        String departmentName,
        Integer semester,
        String division,
        String phone,
        String bleDeviceId,
        boolean active
) {
}
