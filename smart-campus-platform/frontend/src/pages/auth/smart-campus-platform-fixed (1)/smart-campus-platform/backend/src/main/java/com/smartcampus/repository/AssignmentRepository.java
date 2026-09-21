package com.smartcampus.repository;

import com.smartcampus.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByFacultyIdOrderByDeadlineDesc(Long facultyId);

    List<Assignment> findBySubjectDepartmentIdAndSemesterAndDivisionOrderByDeadlineAsc(
            Long departmentId, Integer semester, String division);

    List<Assignment> findAllByOrderByDeadlineDesc();

    long countByDeadlineAfter(LocalDateTime time);
}
