package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Attendance of one student in one session.
 * The unique constraint on (session_id, student_id) is what makes duplicate
 * attendance impossible even if the same detection arrives twice.
 */
@Entity
@Table(name = "attendance_records", uniqueConstraints = @UniqueConstraint(
        name = "uk_attendance_session_student", columnNames = {"session_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AttendanceStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "detection_method", nullable = false, length = 10)
    private DetectionMethod detectionMethod;

    /** Signal strength of the BLE advertisement, when the record came from a real scan. */
    @Column(name = "signal_strength")
    private Integer signalStrength;

    @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt;
}
