package com.pecar.academic.repository;

import com.pecar.academic.entity.Course;
import com.pecar.academic.entity.StudentLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);
    boolean existsByCourseCode(String courseCode);

    List<Course> findByDepartmentId(Long departmentId);
    List<Course> findByLecturerId(Long lecturerId);
    List<Course> findByLevel(StudentLevel level);
    List<Course> findByStatus(Course.CourseStatus status);

    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.courseCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Course> searchCourses(@Param("keyword") String keyword);

    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.student.id = :studentId AND e.status = 'ENROLLED'")
    List<Course> findEnrolledCoursesByStudent(@Param("studentId") Long studentId);
}
