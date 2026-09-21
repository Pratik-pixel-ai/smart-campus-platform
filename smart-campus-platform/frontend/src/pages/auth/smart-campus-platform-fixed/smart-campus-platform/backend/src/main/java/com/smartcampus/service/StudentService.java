package com.smartcampus.service;

import com.smartcampus.dto.common.PageResponse;
import com.smartcampus.dto.people.StudentCreateRequest;
import com.smartcampus.dto.people.StudentResponse;
import com.smartcampus.dto.people.StudentUpdateRequest;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Role;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.mapper.StudentMapper;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    /** Paged, searchable, sortable student list used by the admin and faculty tables. */
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> list(String search, Long departmentId, Integer semester, Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        Page<Student> page = studentRepository.search(term, departmentId, semester, pageable);
        return PageResponse.from(page, StudentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return StudentMapper.toResponse(findStudent(id));
    }

    @Transactional(readOnly = true)
    public StudentResponse me() {
        return StudentMapper.toResponse(currentUser.student());
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> classList(Long departmentId, Integer semester, String division) {
        return studentRepository
                .findByDepartmentIdAndSemesterAndDivision(departmentId, semester, division.toUpperCase())
                .stream()
                .map(StudentMapper::toResponse)
                .toList();
    }

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new DuplicateResourceException("An account already exists for " + request.email());
        }
        if (studentRepository.existsByRollNumber(request.rollNumber())) {
            throw new DuplicateResourceException("Roll number " + request.rollNumber() + " is already registered");
        }
        Department department = findDepartment(request.departmentId());

        User user = userRepository.save(User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_STUDENT)
                .active(true)
                .build());

        Student student = studentRepository.save(Student.builder()
                .user(user)
                .rollNumber(request.rollNumber())
                .department(department)
                .semester(request.semester())
                .division(request.division().toUpperCase())
                .phone(request.phone())
                .bleDeviceId(emptyToNull(request.bleDeviceId()))
                .build());

        return StudentMapper.toResponse(student);
    }

    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        Student student = findStudent(id);
        student.setDepartment(findDepartment(request.departmentId()));
        student.setSemester(request.semester());
        student.setDivision(request.division().toUpperCase());
        student.setPhone(request.phone());
        student.setBleDeviceId(emptyToNull(request.bleDeviceId()));

        User user = student.getUser();
        user.setFullName(request.fullName());
        if (request.active() != null) {
            user.setActive(request.active());
        }

        return StudentMapper.toResponse(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        Student student = findStudent(id);
        User user = student.getUser();
        studentRepository.delete(student);
        userRepository.delete(user);
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
