package com.pecar.academic.repository;

import com.pecar.academic.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByStudentId(String studentId);

    List<Student> findByDepartmentId(Long departmentId);
    List<Student> findByCurrentLevel(Integer level);
    List<Student> findByStatus(Student.StudentStatus status);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email)     LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.studentId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Student> searchStudents(@Param("keyword") String keyword);

    @Query("SELECT COUNT(s) FROM Student s WHERE s.department.id = :deptId")
    long countByDepartment(@Param("deptId") Long deptId);
}
