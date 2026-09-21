package com.smartcampus.service;

import com.smartcampus.dto.assignment.AssignmentRequest;
import com.smartcampus.dto.assignment.AssignmentResponse;
import com.smartcampus.entity.*;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.mapper.AssignmentMapper;
import com.smartcampus.repository.AssignmentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.repository.SubmissionRepository;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final SubjectRepository subjectRepository;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    /** Role-aware list: students see their class's work, faculty see what they set, admins see all. */
    @Transactional(readOnly = true)
    public List<AssignmentResponse> listForCurrentUser() {
        User user = currentUser.user();

        if (user.getRole() == Role.ROLE_STUDENT) {
            Student student = currentUser.student();
            List<Assignment> assignments = assignmentRepository
                    .findBySubjectDepartmentIdAndSemesterAndDivisionOrderByDeadlineAsc(
                            student.getDepartment().getId(), student.getSemester(), student.getDivision());

            // One query for this student's submissions, then a map lookup per assignment
            // instead of a query inside the loop.
            Map<Long, AssignmentSubmission> submissions = submissionRepository
                    .findByStudentIdOrderBySubmittedAtDesc(student.getId()).stream()
                    .collect(Collectors.toMap(s -> s.getAssignment().getId(), Function.identity(), (a, b) -> a));

            return assignments.stream()
                    .map(a -> AssignmentMapper.toResponse(a,
                            submissionRepository.countByAssignmentId(a.getId()),
                            submissions.get(a.getId())))
                    .toList();
        }

        List<Assignment> assignments = user.getRole() == Role.ROLE_FACULTY
                ? assignmentRepository.findByFacultyIdOrderByDeadlineDesc(currentUser.faculty().getId())
                : assignmentRepository.findAllByOrderByDeadlineDesc();

        return assignments.stream()
                .map(a -> AssignmentMapper.toResponse(a, submissionRepository.countByAssignmentId(a.getId()), null))
                .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getById(Long id) {
        Assignment assignment = find(id);
        return AssignmentMapper.toResponse(assignment, submissionRepository.countByAssignmentId(id), null);
    }

    @Transactional
    public AssignmentResponse create(AssignmentRequest request) {
        Faculty faculty = currentUser.faculty();
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));

        Assignment assignment = assignmentRepository.save(Assignment.builder()
                .title(request.title())
                .description(request.description())
                .subject(subject)
                .faculty(faculty)
                .deadline(DateUtils.parseDateTime(request.deadline()))
                .maxMarks(request.maxMarks())
                .attachmentUrl(request.attachmentUrl())
                .semester(request.semester())
                .division(request.division().toUpperCase())
                .build());

        notificationService.notifyClass(subject.getDepartment().getId(), request.semester(),
                request.division().toUpperCase(),
                "New assignment: " + assignment.getTitle(),
                subject.getName() + " - due " + DateUtils.format(assignment.getDeadline()));

        return AssignmentMapper.toResponse(assignment, 0, null);
    }

    @Transactional
    public AssignmentResponse update(Long id, AssignmentRequest request) {
        Assignment assignment = find(id);
        ensureOwner(assignment);

        assignment.setTitle(request.title());
        assignment.setDescription(request.description());
        assignment.setSubject(subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId())));
        assignment.setDeadline(DateUtils.parseDateTime(request.deadline()));
        assignment.setMaxMarks(request.maxMarks());
        assignment.setAttachmentUrl(request.attachmentUrl());
        assignment.setSemester(request.semester());
        assignment.setDivision(request.division().toUpperCase());

        return AssignmentMapper.toResponse(assignmentRepository.save(assignment),
                submissionRepository.countByAssignmentId(id), null);
    }

    @Transactional
    public void delete(Long id) {
        Assignment assignment = find(id);
        ensureOwner(assignment);
        submissionRepository.deleteAll(submissionRepository.findByAssignmentIdOrderBySubmittedAtAsc(id));
        assignmentRepository.delete(assignment);
    }

    public Assignment find(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
    }

    /** Faculty may only edit their own assignments; admins may edit any. */
    private void ensureOwner(Assignment assignment) {
        if (currentUser.isAdmin()) {
            return;
        }
        if (!assignment.getFaculty().getId().equals(currentUser.faculty().getId())) {
            throw new UnauthorizedException("This assignment was created by another faculty member");
        }
    }
}
