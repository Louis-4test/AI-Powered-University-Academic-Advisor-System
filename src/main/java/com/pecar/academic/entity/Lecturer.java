package com.pecar.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lecturers")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lecturer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecturer_id", nullable = false, unique = true)
    private String lecturerId;         // e.g. LEC-2024-001

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String qualification;      // e.g. PhD, MSc

    private String specialization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "lecturer")
    @Builder.Default
    private List<Course> courses = new ArrayList<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
