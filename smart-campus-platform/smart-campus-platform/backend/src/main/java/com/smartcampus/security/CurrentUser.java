package com.smartcampus.security;

import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Resolves the logged-in user from the SecurityContext.
 * Services call this instead of trusting any id sent by the browser.
 */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public String email() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("You are not signed in");
        }
        return authentication.getName();
    }

    public User user() {
        return userRepository.findByEmail(email())
                .orElseThrow(() -> new ResourceNotFoundException("Signed-in user no longer exists"));
    }

    public Student student() {
        return studentRepository.findByUserEmail(email())
                .orElseThrow(() -> new UnauthorizedException("This action is only available to students"));
    }

    public Faculty faculty() {
        return facultyRepository.findByUserEmail(email())
                .orElseThrow(() -> new UnauthorizedException("This action is only available to faculty"));
    }

    public boolean isAdmin() {
        return user().getRole() == com.smartcampus.entity.Role.ROLE_ADMIN;
    }
}
