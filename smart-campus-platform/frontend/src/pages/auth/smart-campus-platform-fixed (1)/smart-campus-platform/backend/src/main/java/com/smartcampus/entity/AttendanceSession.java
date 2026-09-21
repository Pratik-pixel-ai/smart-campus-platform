package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One lecture for which attendance is being taken. Created by a faculty member,
 * stays OPEN while students are detected, then is CLOSED (which writes ABSENT
 * records for everyone who was never detected).
 */
@Entity
@Table(name = "attendance_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "lecture_number", nullable = false)
    private Integer lectureNumber;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false, length = 5)
    private String division;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SessionStatus status;

    /** DEMO or BLE - which AttendanceService implementation created this session. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DetectionMethod mode;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}
