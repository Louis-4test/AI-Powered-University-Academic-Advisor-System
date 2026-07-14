package com.pecar.academic.repository;

import com.pecar.academic.entity.DayOfWeek;
import com.pecar.academic.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findByCourseId(Long courseId);

    List<TimetableEntry> findByLecturerId(Long lecturerId);

    List<TimetableEntry> findByDepartmentId(Long departmentId);

    List<TimetableEntry> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<TimetableEntry> findByLecturerIdAndDayOfWeek(Long lecturerId, DayOfWeek dayOfWeek);

    List<TimetableEntry> findByDepartmentIdAndSemester(Long departmentId, String semester);

    List<TimetableEntry> findByDepartmentIdAndAcademicYearAndSemester(
            Long departmentId, String academicYear, String semester);

    void deleteByCourseId(Long courseId);

    void deleteByLecturerId(Long lecturerId);

    void deleteByDepartmentId(Long departmentId);
}
