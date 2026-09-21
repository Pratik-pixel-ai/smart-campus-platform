package com.smartcampus.repository;

import com.smartcampus.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    boolean existsByRoomNumber(String roomNumber);

    // Explicit HQL string typing prevents PostgreSQL lower(bytea) for null searches.
    @Query("""
            SELECT c FROM Classroom c
            WHERE CAST(:search AS string) IS NULL OR LOWER(c.roomNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                                  OR LOWER(c.building) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            """)
    Page<Classroom> search(@Param("search") String search, Pageable pageable);
}
