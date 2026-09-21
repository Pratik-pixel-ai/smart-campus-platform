package com.smartcampus.dto.auth;

import com.smartcampus.entity.Role;

public record CurrentUserResponse(
        Long userId,
        String fullName,
        String email,
        Role role,
        Long profileId,
        String identifier,
        Long departmentId,
        String departmentName,
        Integer semester,
        String division,
        String phone,
        String designation,
        String bleDeviceId
) {
}
