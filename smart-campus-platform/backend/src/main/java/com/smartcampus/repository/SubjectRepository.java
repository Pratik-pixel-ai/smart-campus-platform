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

    @Query("""
            SELECT s FROM Subject s
            WHERE (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                                   OR LOWER(s.code) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:departmentId IS NULL OR s.department.id = :departmentId)
            """)
    Page<Subject> search(@Param("search") String search,
                         @Param("departmentId") Long departmentId,
                         Pageable pageable);
}
