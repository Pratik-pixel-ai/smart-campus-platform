package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_submissions", uniqueConstraints = @UniqueConstraint(
        name = "uk_submission_assignment_student", columnNames = {"assignment_id", "student_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "submission_url", nullable = false, length = 500)
    private String submissionUrl;

    @Column(length = 500)
    private String remarks;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private SubmissionStatus status;

    @Column(name = "marks_obtained")
    private Integer marksObtained;

    @Column(length = 1000)
    private String feedback;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
}
