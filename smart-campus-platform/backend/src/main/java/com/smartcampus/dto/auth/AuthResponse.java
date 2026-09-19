package com.smartcampus.dto.auth;

import com.smartcampus.entity.Role;

/** Issued on login/register. The token is a signed JWT; no password is ever returned. */
public record AuthResponse(
        String token,
        Long userId,
        String fullName,
        String email,
        Role role,
        Long profileId,
        String departmentName
) {
}
