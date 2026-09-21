package com.smartcampus.service;

import com.smartcampus.dto.auth.AuthResponse;
import com.smartcampus.dto.auth.CurrentUserResponse;
import com.smartcampus.dto.auth.LoginRequest;
import com.smartcampus.dto.auth.RegisterRequest;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration and login.
 * Passwords are hashed with BCrypt before they ever reach the database, and the
 * response carries a JWT instead of a server-side session.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CurrentUser currentUser;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ROLE_ADMIN) {
            throw new BadRequestException("Administrator accounts cannot be self-registered");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account already exists for " + request.email());
        }

        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));

        User user = userRepository.save(User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(true)
                .build());

        Long profileId;
        if (request.role() == Role.ROLE_STUDENT) {
            if (request.rollNumber() == null || request.rollNumber().isBlank()
                    || request.semester() == null || request.division() == null || request.division().isBlank()) {
                throw new BadRequestException("Roll number, semester and division are required for students");
            }
            if (studentRepository.existsByRollNumber(request.rollNumber())) {
                throw new DuplicateResourceException("Roll number " + request.rollNumber() + " is already registered");
            }
            Student student = studentRepository.save(Student.builder()
                    .user(user)
                    .rollNumber(request.rollNumber())
                    .department(department)
                    .semester(request.semester())
                    .division(request.division().toUpperCase())
                    .build());
            profileId = student.getId();
        } else {
            if (request.employeeCode() == null || request.employeeCode().isBlank()) {
                throw new BadRequestException("Employee code is required for faculty");
            }
            if (facultyRepository.existsByEmployeeCode(request.employeeCode())) {
                throw new DuplicateResourceException("Employee code " + request.employeeCode() + " is already registered");
            }
            Faculty faculty = facultyRepository.save(Faculty.builder()
                    .user(user)
                    .employeeCode(request.employeeCode())
                    .department(department)
                    .designation(request.designation() == null ? "Assistant Professor" : request.designation())
                    .build());
            profileId = faculty.getId();
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(),
                user.getRole(), profileId, department.getName());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException (handled globally as 401) when the password is wrong.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));

        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found for " + request.email()));

        Long profileId = null;
        String departmentName = null;
        if (user.getRole() == Role.ROLE_STUDENT) {
            Student student = studentRepository.findByUserId(user.getId()).orElse(null);
            if (student != null) {
                profileId = student.getId();
                departmentName = student.getDepartment().getName();
            }
        } else if (user.getRole() == Role.ROLE_FACULTY) {
            Faculty faculty = facultyRepository.findByUserId(user.getId()).orElse(null);
            if (faculty != null) {
                profileId = faculty.getId();
                departmentName = faculty.getDepartment().getName();
            }
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(),
                user.getRole(), profileId, departmentName);
    }

    /** Profile of whoever owns the JWT on this request. */
    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser() {
        User user = currentUser.user();

        if (user.getRole() == Role.ROLE_STUDENT) {
            Student s = studentRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile is missing for this account"));
            return new CurrentUserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                    s.getId(), s.getRollNumber(), s.getDepartment().getId(), s.getDepartment().getName(),
                    s.getSemester(), s.getDivision(), s.getPhone(), null, s.getBleDeviceId());
        }
        if (user.getRole() == Role.ROLE_FACULTY) {
            Faculty f = facultyRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty profile is missing for this account"));
            return new CurrentUserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                    f.getId(), f.getEmployeeCode(), f.getDepartment().getId(), f.getDepartment().getName(),
                    null, null, f.getPhone(), f.getDesignation(), null);
        }
        return new CurrentUserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                null, "ADMIN", null, "Campus administration", null, null, null, "Administrator", null);
    }
}
