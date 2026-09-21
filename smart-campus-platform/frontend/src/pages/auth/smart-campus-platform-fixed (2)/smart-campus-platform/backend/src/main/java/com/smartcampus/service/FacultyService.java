package com.smartcampus.service;

import com.smartcampus.dto.common.PageResponse;
import com.smartcampus.dto.people.FacultyCreateRequest;
import com.smartcampus.dto.people.FacultyResponse;
import com.smartcampus.dto.people.FacultyUpdateRequest;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Role;
import com.smartcampus.entity.User;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.mapper.FacultyMapper;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public PageResponse<FacultyResponse> list(String search, Long departmentId, Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return PageResponse.from(facultyRepository.search(term, departmentId, pageable), FacultyMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<FacultyResponse> listAll() {
        return facultyRepository.findAll().stream().map(FacultyMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FacultyResponse getById(Long id) {
        return FacultyMapper.toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public FacultyResponse me() {
        return FacultyMapper.toResponse(currentUser.faculty());
    }

    @Transactional
    public FacultyResponse create(FacultyCreateRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new DuplicateResourceException("An account already exists for " + request.email());
        }
        if (facultyRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new DuplicateResourceException("Employee code " + request.employeeCode() + " is already registered");
        }
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId()));

        User user = userRepository.save(User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_FACULTY)
                .active(true)
                .build());

        Faculty faculty = facultyRepository.save(Faculty.builder()
                .user(user)
                .employeeCode(request.employeeCode())
                .department(department)
                .designation(request.designation() == null ? "Assistant Professor" : request.designation())
                .phone(request.phone())
                .build());

        return FacultyMapper.toResponse(faculty);
    }

    @Transactional
    public FacultyResponse update(Long id, FacultyUpdateRequest request) {
        Faculty faculty = find(id);
        faculty.setDepartment(departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId())));
        faculty.setDesignation(request.designation());
        faculty.setPhone(request.phone());

        User user = faculty.getUser();
        user.setFullName(request.fullName());
        if (request.active() != null) {
            user.setActive(request.active());
        }
        return FacultyMapper.toResponse(facultyRepository.save(faculty));
    }

    @Transactional
    public void delete(Long id) {
        Faculty faculty = find(id);
        User user = faculty.getUser();
        facultyRepository.delete(faculty);
        userRepository.delete(user);
    }

    private Faculty find(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", id));
    }
}
