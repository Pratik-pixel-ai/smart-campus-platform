package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "classrooms", uniqueConstraints = @UniqueConstraint(name = "uk_classrooms_room_number", columnNames = "room_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classroom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(nullable = false, length = 60)
    private String building;

    @Column(nullable = false)
    private Integer capacity;
}
