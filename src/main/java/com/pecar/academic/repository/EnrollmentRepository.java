package com.pecar.academic.repository;

import com.pecar.academic.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByCourseId(Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseIdAndAcademicYearAndSemester(
            Long studentId, Long courseId, String academicYear, String semester);

    boolean existsByStudentIdAndCourseIdAndAcademicYearAndSemester(
            Long studentId, Long courseId, String academicYear, String semester);

    long countByCourseId(Long courseId);
}
