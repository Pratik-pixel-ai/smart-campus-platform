package com.smartcampus.repository;

import com.smartcampus.entity.AttendanceSession;
import com.smartcampus.entity.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    List<AttendanceSession> findByFacultyIdOrderByStartedAtDesc(Long facultyId);

    Page<AttendanceSession> findAllByOrderByStartedAtDesc(Pageable pageable);

    long countByFacultyIdAndSessionDate(Long facultyId, LocalDate date);

    long countByStatus(SessionStatus status);

    List<AttendanceSession> findBySubjectIdAndSemesterAndDivision(Long subjectId, Integer semester, String division);
}
