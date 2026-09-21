package com.smartcampus.repository;

import com.smartcampus.entity.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByCode(String code);

    List<Subject> findByDepartmentIdAndSemester(Long departmentId, Integer semester);

    List<Subject> findByFacultyId(Long facultyId);

    // Explicit HQL string typing prevents PostgreSQL lower(bytea) for null searches.
    @Query("""
            SELECT s FROM Subject s
            WHERE (CAST(:search AS string) IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                                   OR LOWER(s.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
              AND (:departmentId IS NULL OR s.department.id = :departmentId)
            """)
    Page<Subject> search(@Param("search") String search,
                         @Param("departmentId") Long departmentId,
                         Pageable pageable);
}
