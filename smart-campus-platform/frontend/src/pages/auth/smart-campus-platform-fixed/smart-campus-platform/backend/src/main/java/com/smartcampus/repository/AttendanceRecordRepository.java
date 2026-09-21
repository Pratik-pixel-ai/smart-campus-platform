package com.smartcampus.repository;

import com.smartcampus.dto.attendance.SubjectAttendanceProjection;
import com.smartcampus.entity.AttendanceRecord;
import com.smartcampus.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findBySessionIdAndStudentId(Long sessionId, Long studentId);

    boolean existsBySessionIdAndStudentId(Long sessionId, Long studentId);

    List<AttendanceRecord> findBySessionIdOrderByMarkedAtAsc(Long sessionId);

    List<AttendanceRecord> findByStudentIdOrderByMarkedAtDesc(Long studentId);

    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    long countByStudentId(Long studentId);

    long countByStatus(AttendanceStatus status);

    long countByStudentDepartmentId(Long departmentId);

    long countByStudentDepartmentIdAndStatus(Long departmentId, AttendanceStatus status);

    /**
     * Subject-wise attendance for one student, aggregated by the database rather than
     * by looping in Java: one query instead of N.
     */
    @Query("""
            SELECT r.session.subject.id AS subjectId,
                   r.session.subject.name AS subjectName,
                   r.session.subject.code AS subjectCode,
                   COUNT(r) AS totalLectures,
                   SUM(CASE WHEN r.status = com.smartcampus.entity.AttendanceStatus.PRESENT THEN 1 ELSE 0 END) AS presentLectures
            FROM AttendanceRecord r
            WHERE r.student.id = :studentId
            GROUP BY r.session.subject.id, r.session.subject.name, r.session.subject.code
            ORDER BY r.session.subject.name
            """)
    List<SubjectAttendanceProjection> findSubjectWiseAttendance(@Param("studentId") Long studentId);
}
