package com.smartcampus.service;

import com.smartcampus.dto.master.DepartmentRequest;
import com.smartcampus.dto.master.DepartmentResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.mapper.MasterDataMapper;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listAll() {
        return departmentRepository.findAll().stream()
                .map(department -> MasterDataMapper.toResponse(
                        department,
                        studentRepository.findByDepartmentId(department.getId()).size(),
                        facultyRepository.search(null, department.getId(),
                                org.springframework.data.domain.Pageable.unpaged()).getTotalElements()))
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department department = find(id);
        return MasterDataMapper.toResponse(department,
                studentRepository.findByDepartmentId(id).size(),
                facultyRepository.search(null, id, org.springframework.data.domain.Pageable.unpaged())
                        .getTotalElements());
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.code().toUpperCase())) {
            throw new DuplicateResourceException("Department code " + request.code() + " already exists");
        }
        if (departmentRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Department " + request.name() + " already exists");
        }
        Department saved = departmentRepository.save(Department.builder()
                .name(request.name())
                .code(request.code().toUpperCase())
                .hodName(request.hodName())
                .build());
        return MasterDataMapper.toResponse(saved, 0, 0);
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department department = find(id);
        department.setName(request.name());
        department.setCode(request.code().toUpperCase());
        department.setHodName(request.hodName());
        departmentRepository.save(department);
        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        Department department = find(id);
        // Referential integrity is easier to explain as a clear message than as a database error.
        if (!studentRepository.findByDepartmentId(id).isEmpty()) {
            throw new BadRequestException("Move or remove the students in this department first");
        }
        if (!subjectRepository.search(null, id, org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
            throw new BadRequestException("Remove the subjects in this department first");
        }
        departmentRepository.delete(department);
    }

    private Department find(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }
}
