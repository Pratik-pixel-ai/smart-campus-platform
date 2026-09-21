package com.smartcampus.repository;

import com.smartcampus.entity.AcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {

    List<AcademicRecord> findByStudentIdOrderBySemesterAscSubjectNameAsc(Long studentId);

    List<AcademicRecord> findByStudentIdAndSemester(Long studentId, Integer semester);

    Optional<AcademicRecord> findByStudentIdAndSubjectIdAndSemester(Long studentId, Long subjectId, Integer semester);

    List<AcademicRecord> findBySubjectId(Long subjectId);
}
