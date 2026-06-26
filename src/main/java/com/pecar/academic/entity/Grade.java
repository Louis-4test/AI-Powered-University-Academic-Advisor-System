package com.pecar.academic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grades",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id", "academic_year", "semester"}))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(nullable = false)
    private String semester;

    // Component scores (out of 100)
    @Builder.Default
    private Double attendance = 0.0;

    @Builder.Default
    private Double assignments = 0.0;

    @Builder.Default
    private Double projects = 0.0;

    @Builder.Default
    private Double tests = 0.0;

    @Builder.Default
    private Double exams = 0.0;

    // Calculated
    @Column(name = "total_score")
    private Double totalScore;

    @Column(name = "letter_grade")
    private String letterGrade;        // A, B+, B, C+, C, D, F

    @Column(name = "grade_point")
    private Double gradePoint;         // 4.0, 3.5, 3.0, 2.5, 2.0, 1.0, 0.0

    // ── Grade computation ──────────────────────────────────────────────────────

    @PrePersist
    @PreUpdate
    public void computeGrade() {
        // Weighted formula: Attendance 10%, Assignments 15%, Projects 15%, Tests 20%, Exams 40%
        this.totalScore = (attendance * 0.10)
                        + (assignments * 0.15)
                        + (projects    * 0.15)
                        + (tests       * 0.20)
                        + (exams       * 0.40);

        if      (totalScore >= 90) { letterGrade = "A";  gradePoint = 4.0; }
        else if (totalScore >= 80) { letterGrade = "B+"; gradePoint = 3.5; }
        else if (totalScore >= 70) { letterGrade = "B";  gradePoint = 3.0; }
        else if (totalScore >= 65) { letterGrade = "C+"; gradePoint = 2.5; }
        else if (totalScore >= 55) { letterGrade = "C";  gradePoint = 2.0; }
        else if (totalScore >= 45) { letterGrade = "D";  gradePoint = 1.0; }
        else                       { letterGrade = "F";  gradePoint = 0.0; }
    }
}
