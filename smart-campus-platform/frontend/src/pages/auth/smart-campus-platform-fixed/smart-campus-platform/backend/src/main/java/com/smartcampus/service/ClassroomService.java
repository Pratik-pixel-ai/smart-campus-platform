package com.smartcampus.service;

import com.smartcampus.dto.common.PageResponse;
import com.smartcampus.dto.master.ClassroomRequest;
import com.smartcampus.dto.master.ClassroomResponse;
import com.smartcampus.entity.Classroom;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.mapper.MasterDataMapper;
import com.smartcampus.repository.ClassroomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;

    @Transactional(readOnly = true)
    public PageResponse<ClassroomResponse> list(String search, Pageable pageable) {
        String term = (search == null || search.isBlank()) ? null : search.trim();
        return PageResponse.from(classroomRepository.search(term, pageable), MasterDataMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ClassroomResponse> listAll() {
        return classroomRepository.findAll().stream().map(MasterDataMapper::toResponse).toList();
    }

    @Transactional
    public ClassroomResponse create(ClassroomRequest request) {
        if (classroomRepository.existsByRoomNumber(request.roomNumber())) {
            throw new DuplicateResourceException("Room " + request.roomNumber() + " already exists");
        }
        return MasterDataMapper.toResponse(classroomRepository.save(Classroom.builder()
                .roomNumber(request.roomNumber())
                .building(request.building())
                .capacity(request.capacity())
                .build()));
    }

    @Transactional
    public ClassroomResponse update(Long id, ClassroomRequest request) {
        Classroom classroom = find(id);
        classroom.setRoomNumber(request.roomNumber());
        classroom.setBuilding(request.building());
        classroom.setCapacity(request.capacity());
        return MasterDataMapper.toResponse(classroomRepository.save(classroom));
    }

    @Transactional
    public void delete(Long id) {
        classroomRepository.delete(find(id));
    }

    private Classroom find(Long id) {
        return classroomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", id));
    }
}
