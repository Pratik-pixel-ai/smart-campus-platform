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

    @Query("""
            SELECT f FROM Faculty f
            JOIN f.user u
            WHERE (:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                                   OR LOWER(f.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:departmentId IS NULL OR f.department.id = :departmentId)
            """)
    Page<Faculty> search(@Param("search") String search,
                         @Param("departmentId") Long departmentId,
                         Pageable pageable);
}
