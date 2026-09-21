package com.smartcampus.service;

import com.smartcampus.dto.assignment.GradeRequest;
import com.smartcampus.dto.assignment.SubmissionRequest;
import com.smartcampus.dto.assignment.SubmissionResponse;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.mapper.AssignmentMapper;
import com.smartcampus.repository.SubmissionRepository;
import com.smartcampus.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentService assignmentService;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    /** Submitting twice updates the existing submission rather than creating a duplicate. */
    @Transactional
    public SubmissionResponse submit(SubmissionRequest request) {
        Student student = currentUser.student();
        Assignment assignment = assignmentService.find(request.assignmentId());

        boolean sameClass = student.getDepartment().getId().equals(assignment.getSubject().getDepartment().getId())
                && student.getSemester().equals(assignment.getSemester())
                && student.getDivision().equalsIgnoreCase(assignment.getDivision());
        if (!sameClass) {
            throw new UnauthorizedException("This assignment was not set for your class");
        }

        AssignmentSubmission submission = submissionRepository
                .findByAssignmentIdAndStudentId(assignment.getId(), student.getId())
                .orElseGet(() -> AssignmentSubmission.builder()
                        .assignment(assignment)
                        .student(student)
                        .build());

        if (submission.getStatus() == SubmissionStatus.GRADED) {
            throw new BadRequestException("This assignment has already been graded and cannot be resubmitted");
        }

        LocalDateTime now = LocalDateTime.now();
        submission.setSubmissionUrl(request.submissionUrl());
        submission.setRemarks(request.remarks());
        submission.setSubmittedAt(now);
        submission.setStatus(now.isAfter(assignment.getDeadline()) ? SubmissionStatus.LATE : SubmissionStatus.SUBMITTED);

        return AssignmentMapper.toResponse(submissionRepository.save(submission));
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> mySubmissions() {
        return submissionRepository.findByStudentIdOrderBySubmittedAtDesc(currentUser.student().getId())
                .stream().map(AssignmentMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> forAssignment(Long assignmentId) {
        Assignment assignment = assignmentService.find(assignmentId);
        if (!currentUser.isAdmin() && !assignment.getFaculty().getId().equals(currentUser.faculty().getId())) {
            throw new UnauthorizedException("This assignment was created by another faculty member");
        }
        return submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(assignmentId)
                .stream().map(AssignmentMapper::toResponse).toList();
    }

    @Transactional
    public SubmissionResponse grade(Long submissionId, GradeRequest request) {
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", submissionId));

        Assignment assignment = submission.getAssignment();
        if (!currentUser.isAdmin() && !assignment.getFaculty().getId().equals(currentUser.faculty().getId())) {
            throw new UnauthorizedException("This assignment was created by another faculty member");
        }
        if (request.marksObtained() > assignment.getMaxMarks()) {
            throw new BadRequestException("Marks cannot exceed " + assignment.getMaxMarks());
        }

        submission.setMarksObtained(request.marksObtained());
        submission.setFeedback(request.feedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(LocalDateTime.now());

        notificationService.notifyUser(submission.getStudent().getUser(),
                "Assignment graded: " + assignment.getTitle(),
                "You scored " + request.marksObtained() + " out of " + assignment.getMaxMarks());

        return AssignmentMapper.toResponse(submissionRepository.save(submission));
    }
}
