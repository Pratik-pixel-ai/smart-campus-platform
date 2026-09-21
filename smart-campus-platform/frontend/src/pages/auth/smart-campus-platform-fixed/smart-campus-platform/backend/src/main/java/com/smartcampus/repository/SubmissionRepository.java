package com.smartcampus.repository;

import com.smartcampus.entity.AssignmentSubmission;
import com.smartcampus.entity.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<AssignmentSubmission> findByAssignmentIdOrderBySubmittedAtAsc(Long assignmentId);

    List<AssignmentSubmission> findByStudentIdOrderBySubmittedAtDesc(Long studentId);

    long countByAssignmentFacultyIdAndStatusNot(Long facultyId, SubmissionStatus status);

    long countByAssignmentId(Long assignmentId);
}
