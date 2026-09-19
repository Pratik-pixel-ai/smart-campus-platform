package com.smartcampus.service;

import com.smartcampus.dto.academic.AcademicRecordRequest;
import com.smartcampus.dto.academic.AcademicRecordResponse;
import com.smartcampus.dto.academic.AcademicSummaryResponse;
import com.smartcampus.dto.academic.SemesterSummaryResponse;
import com.smartcampus.entity.*;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedException;
import com.smartcampus.mapper.AcademicMapper;
import com.smartcampus.repository.AcademicRecordRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.security.CurrentUser;
import com.smartcampus.util.GradeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Marks and grades. total and grade are derived from the internal and external marks
 * so the stored values can never disagree with each other.
 */
@Service
@RequiredArgsConstructor
public class AcademicService {

    private static final int MAX_MARKS_PER_SUBJECT = 100;

    private final AcademicRecordRepository academicRecordRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public AcademicSummaryResponse mySummary() {
        return buildSummary(currentUser.student());
    }

    @Transactional(readOnly = true)
    public AcademicSummaryResponse summaryForStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        ensureCanView(student);
        return buildSummary(student);
    }

    @Transactional
    public AcademicRecordResponse save(AcademicRecordRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.studentId()));
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", request.subjectId()));

        // Upsert: one record per student + subject + semester, enforced by a unique constraint.
        AcademicRecord record = academicRecordRepository
                .findByStudentIdAndSubjectIdAndSemester(student.getId(), subject.getId(), request.semester())
                .orElseGet(() -> AcademicRecord.builder()
                        .student(student)
                        .subject(subject)
                        .semester(request.semester())
                        .build());

        int total = request.internalMarks() + request.externalMarks();
        record.setInternalMarks(request.internalMarks());
        record.setExternalMarks(request.externalMarks());
        record.setTotalMarks(total);
        record.setGrade(GradeCalculator.gradeFor(total));

        return AcademicMapper.toResponse(academicRecordRepository.save(record));
    }

    @Transactional
    public void delete(Long id) {
        AcademicRecord record = academicRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic record", id));
        academicRecordRepository.delete(record);
    }

    private AcademicSummaryResponse buildSummary(Student student) {
        List<AcademicRecord> records = academicRecordRepository
                .findByStudentIdOrderBySemesterAscSubjectNameAsc(student.getId());

        // Group by semester, keeping semesters in order (TreeMap) so the UI needs no sorting.
        Map<Integer, List<AcademicRecord>> bySemester = records.stream()
                .collect(Collectors.groupingBy(AcademicRecord::getSemester, TreeMap::new, Collectors.toList()));

        List<SemesterSummaryResponse> semesters = bySemester.entrySet().stream()
                .map(entry -> {
                    int scored = entry.getValue().stream().mapToInt(AcademicRecord::getTotalMarks).sum();
                    int possible = entry.getValue().size() * MAX_MARKS_PER_SUBJECT;
                    return new SemesterSummaryResponse(
                            entry.getKey(),
                            entry.getValue().size(),
                            scored,
                            possible,
                            GradeCalculator.percentage(scored, possible),
                            entry.getValue().stream().map(AcademicMapper::toResponse).toList());
                })
                .toList();

        int totalScored = records.stream().mapToInt(AcademicRecord::getTotalMarks).sum();
        int totalPossible = records.size() * MAX_MARKS_PER_SUBJECT;

        return new AcademicSummaryResponse(
                student.getId(),
                student.getUser().getFullName(),
                student.getRollNumber(),
                GradeCalculator.percentage(totalScored, totalPossible),
                semesters
        );
    }

    private void ensureCanView(Student student) {
        User user = currentUser.user();
        if (user.getRole() == Role.ROLE_STUDENT && !student.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You can only view your own academic record");
        }
    }
}
