package com.smartcampus.repository;

import com.smartcampus.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    Optional<Faculty> findByUserId(Long userId);

    Optional<Faculty> findByUserEmail(String email);

    boolean existsByEmployeeCode(String employeeCode);

    // Explicit HQL string typing prevents PostgreSQL lower(bytea) for null searches.
    @Query("""
            SELECT f FROM Faculty f
            JOIN f.user u
            WHERE (CAST(:search AS string) IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                                   OR LOWER(f.employeeCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
              AND (:departmentId IS NULL OR f.department.id = :departmentId)
            """)
    Page<Faculty> search(@Param("search") String search,
                         @Param("departmentId") Long departmentId,
                         Pageable pageable);
}
