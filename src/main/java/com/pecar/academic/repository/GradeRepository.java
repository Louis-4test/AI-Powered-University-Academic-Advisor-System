package com.pecar.academic.repository;

import com.pecar.academic.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByStudentId(Long studentId);
    List<Grade> findByStudentIdAndAcademicYear(Long studentId, String academicYear);
    List<Grade> findByCourseId(Long courseId);

    Optional<Grade> findByStudentIdAndCourseIdAndAcademicYearAndSemester(
            Long studentId, Long courseId, String academicYear, String semester);

    // Semester GPA
    @Query("SELECT AVG(g.gradePoint) FROM Grade g " +
           "WHERE g.student.id = :studentId " +
           "AND g.academicYear = :year AND g.semester = :semester")
    Optional<Double> calculateSemesterGPA(
            @Param("studentId") Long studentId,
            @Param("year") String year,
            @Param("semester") String semester);

    // Cumulative GPA (CGPA)
    @Query("SELECT AVG(g.gradePoint) FROM Grade g WHERE g.student.id = :studentId")
    Optional<Double> calculateCGPA(@Param("studentId") Long studentId);

    // Students with GPA above threshold — used by Streams API demo
    @Query("SELECT g.student.id, AVG(g.gradePoint) as gpa FROM Grade g " +
           "GROUP BY g.student.id HAVING AVG(g.gradePoint) >= :threshold")
    List<Object[]> findStudentsWithCGPAAbove(@Param("threshold") Double threshold);

    // At-risk students (CGPA below threshold)
    @Query("SELECT g.student.id, AVG(g.gradePoint) as gpa FROM Grade g " +
           "GROUP BY g.student.id HAVING AVG(g.gradePoint) < :threshold")
    List<Object[]> findAtRiskStudents(@Param("threshold") Double threshold);
}
