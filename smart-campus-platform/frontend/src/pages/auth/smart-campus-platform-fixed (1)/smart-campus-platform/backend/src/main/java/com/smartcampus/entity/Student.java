package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Academic profile of a ROLE_STUDENT user.
 * One-to-one with User, many-to-one with Department.
 */
@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(name = "uk_students_roll_number", columnNames = "roll_number"),
        @UniqueConstraint(name = "uk_students_user", columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "roll_number", nullable = false, length = 30)
    private String rollNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private Integer semester;

    @Column(nullable = false, length = 5)
    private String division;

    @Column(length = 20)
    private String phone;

    /**
     * Identifier advertised by the student's phone over BLE.
     * Used by BleAttendanceService to map a scanned advertisement back to a student.
     */
    @Column(name = "ble_device_id", length = 64, unique = true)
    private String bleDeviceId;
}
