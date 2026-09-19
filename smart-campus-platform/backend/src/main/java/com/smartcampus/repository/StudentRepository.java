package com.smartcampus.repository;

import com.smartcampus.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByUserEmail(String email);

    Optional<Student> findByRollNumber(String rollNumber);

    Optional<Student> findByBleDeviceId(String bleDeviceId);

    boolean existsByRollNumber(String rollNumber);

    /**
     * Admin/faculty student table: free-text search on name or roll number plus an
     * optional department filter. Paging and sorting are handled by Spring Data.
     */
    @Query("""
            SELECT s FROM Student s
            JOIN s.user u
            WHERE (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                                   OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:departmentId IS NULL OR s.department.id = :departmentId)
              AND (:semester IS NULL OR s.semester = :semester)
            """)
    Page<Student> search(@Param("search") String search,
                         @Param("departmentId") Long departmentId,
                         @Param("semester") Integer semester,
                         Pageable pageable);

    /** The class list for a lecture: same department, semester and division. */
    List<Student> findByDepartmentIdAndSemesterAndDivision(Long departmentId, Integer semester, String division);

    List<Student> findByDepartmentId(Long departmentId);
}
