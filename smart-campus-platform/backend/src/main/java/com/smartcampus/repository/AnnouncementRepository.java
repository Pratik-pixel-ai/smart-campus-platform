package com.smartcampus.repository;

import com.smartcampus.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByCreatedAtDesc();

    /** Campus-wide announcements (null department) plus the ones for the user's department. */
    @Query("""
            SELECT a FROM Announcement a
            WHERE a.department IS NULL OR a.department.id = :departmentId
            ORDER BY a.createdAt DESC
            """)
    List<Announcement> findVisibleForDepartment(@Param("departmentId") Long departmentId);

    List<Announcement> findByCreatedByIdOrderByCreatedAtDesc(Long userId);
}
