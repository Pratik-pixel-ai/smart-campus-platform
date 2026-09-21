package com.smartcampus.service;

import com.smartcampus.dto.common.PageResponse;
import com.smartcampus.dto.master.SubjectRequest;
import com.smartcampus.dto.master.SubjectResponse;
import com.smartcampus.entity.Subject;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.mapper.MasterDataMapper;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;

    @Transactional(readOnly = true)
    public PageResponse<SubjectResponse> list(String search, Long departmentId, Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return PageResponse.from(subjectRepository.search(term, departmentId, pageable), MasterDataMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> listAll() {
        return subjectRepository.findAll().stream().map(MasterDataMapper::toResponse).toList();
    }

    /** Subjects a faculty member teaches - used to fill the attendance and assignment forms. */
    @Transactional(readOnly = true)
    public List<SubjectResponse> listForFaculty(Long facultyId) {
        return subjectRepository.findByFacultyId(facultyId).stream().map(MasterDataMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> listForClass(Long departmentId, Integer semester) {
        return subjectRepository.findByDepartmentIdAndSemester(departmentId, semester).stream()
                .map(MasterDataMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubjectResponse getById(Long id) {
        return MasterDataMapper.toResponse(find(id));
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        if (subjectRepository.existsByCode(request.code().toUpperCase())) {
            throw new DuplicateResourceException("Subject code " + request.code() + " already exists");
        }
        Subject subject = Subject.builder()
                .name(request.name())
                .code(request.code().toUpperCase())
                .department(departmentRepository.findById(request.departmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId())))
                .semester(request.semester())
                .credits(request.credits())
                .build();
        applyFaculty(subject, request.facultyId());
        return MasterDataMapper.toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = find(id);
        subject.setName(request.name());
        subject.setCode(request.code().toUpperCase());
        subject.setDepartment(departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", request.departmentId())));
        subject.setSemester(request.semester());
        subject.setCredits(request.credits());
        applyFaculty(subject, request.facultyId());
        return MasterDataMapper.toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public void delete(Long id) {
        subjectRepository.delete(find(id));
    }

    public Subject find(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
    }

    private void applyFaculty(Subject subject, Long facultyId) {
        if (facultyId == null) {
            subject.setFaculty(null);
            return;
        }
        subject.setFaculty(facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", facultyId)));
    }
}
