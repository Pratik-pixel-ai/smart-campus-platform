package com.smartcampus.mapper;

import com.smartcampus.dto.assignment.AssignmentResponse;
import com.smartcampus.dto.assignment.SubmissionResponse;
import com.smartcampus.entity.Assignment;
import com.smartcampus.entity.AssignmentSubmission;
import com.smartcampus.util.DateUtils;

import java.time.LocalDateTime;

public final class AssignmentMapper {

    private AssignmentMapper() {
    }

    public static AssignmentResponse toResponse(Assignment assignment, long submissionCount,
                                                AssignmentSubmission studentSubmission) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getSubject().getId(),
                assignment.getSubject().getName(),
                assignment.getSubject().getCode(),
                assignment.getFaculty().getId(),
                assignment.getFaculty().getUser().getFullName(),
                DateUtils.format(assignment.getDeadline()),
                assignment.getMaxMarks(),
                assignment.getAttachmentUrl(),
                assignment.getSemester(),
                assignment.getDivision(),
                submissionCount,
                studentSubmission == null ? "PENDING" : studentSubmission.getStatus().name(),
                studentSubmission == null ? null : studentSubmission.getMarksObtained(),
                assignment.getDeadline().isBefore(LocalDateTime.now())
        );
    }

    public static SubmissionResponse toResponse(AssignmentSubmission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getAssignment().getTitle(),
                submission.getAssignment().getMaxMarks(),
                submission.getStudent().getId(),
                submission.getStudent().getUser().getFullName(),
                submission.getStudent().getRollNumber(),
                submission.getSubmissionUrl(),
                submission.getRemarks(),
                DateUtils.format(submission.getSubmittedAt()),
                submission.getStatus().name(),
                submission.getMarksObtained(),
                submission.getFeedback(),
                DateUtils.format(submission.getGradedAt())
        );
    }
}
