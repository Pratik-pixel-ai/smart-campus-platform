package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Marks of one student in one subject for one semester.
 * total and grade are derived in AcademicService so the rules live in one place.
 */
@Entity
@Table(name = "academic_records", uniqueConstraints = @UniqueConstraint(
        name = "uk_academic_student_subject_sem", columnNames = {"student_id", "subject_id", "semester"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false)
    private Integer semester;

    @Column(name = "internal_marks", nullable = false)
    private Integer internalMarks;

    @Column(name = "external_marks", nullable = false)
    private Integer externalMarks;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(nullable = false, length = 3)
    private String grade;
}
