package com.smartcampus.repository;

import com.smartcampus.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    boolean existsByRoomNumber(String roomNumber);

    @Query("""
            SELECT c FROM Classroom c
            WHERE :search IS NULL OR LOWER(c.roomNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                                  OR LOWER(c.building) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Classroom> search(@Param("search") String search, Pageable pageable);
}
