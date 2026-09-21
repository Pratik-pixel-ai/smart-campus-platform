package com.smartcampus.entity;

/**
 * Application roles. Stored as a string column on the users table and exposed to
 * Spring Security as a GrantedAuthority, which is why the ROLE_ prefix is kept here.
 */
public enum Role {
    ROLE_STUDENT,
    ROLE_FACULTY,
    ROLE_ADMIN
}
