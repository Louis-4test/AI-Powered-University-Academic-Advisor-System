package com.pecar.academic.repository;

import com.pecar.academic.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LecturerRepository extends JpaRepository<Lecturer, Long> {

    Optional<Lecturer> findByLecturerId(String lecturerId);
    Optional<Lecturer> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Lecturer> findByDepartmentId(Long departmentId);

    @Query("SELECT l FROM Lecturer l WHERE " +
           "LOWER(l.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.email)     LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Lecturer> searchLecturers(@Param("keyword") String keyword);
}
