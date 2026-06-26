package com.pecar.academic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String code;

    private String description;

    @OneToMany(mappedBy = "department")
    @Builder.Default
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    @Builder.Default
    private List<Lecturer> lecturers = new ArrayList<>();

    @OneToMany(mappedBy = "department")
    @Builder.Default
    private List<Course> courses = new ArrayList<>();
}
