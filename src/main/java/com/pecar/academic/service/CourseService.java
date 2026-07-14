package com.pecar.academic.service;

import com.pecar.academic.dto.CourseDTO;
import com.pecar.academic.entity.*;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository      courseRepository;
    private final DepartmentRepository  departmentRepository;
    private final LecturerRepository    lecturerRepository;
    private final EnrollmentRepository  enrollmentRepository;
    private final TimetableRepository   timetableRepository;

    @Transactional
    public CourseDTO.Response createCourse(CourseDTO.Request req) {
        if (courseRepository.existsByCourseCode(req.getCourseCode())) {
            throw new DuplicateResourceException("Course code " + req.getCourseCode() + " already exists");
        }

        Department dept = req.getDepartmentId() != null
                ? departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"))
                : null;

        Lecturer lecturer = req.getLecturerId() != null
                ? lecturerRepository.findById(req.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"))
                : null;

        Course course = Course.builder()
                .courseCode(req.getCourseCode())
                .title(req.getTitle())
                .description(req.getDescription())
                .creditHours(req.getCreditHours())
                .level(req.getLevel())
                .semester(req.getSemester())
                .department(dept)
                .lecturer(lecturer)
                .build();

        return toResponse(courseRepository.save(course));
    }

    public CourseDTO.Response getCourseById(Long id) {
        return toResponse(findById(id));
    }

    public List<CourseDTO.Response> getAllCourses() {
        return courseRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CourseDTO.Response> searchCourses(String keyword) {
        return courseRepository.searchCourses(keyword).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<CourseDTO.Response> getCoursesByStudent(Long studentId) {
        return courseRepository.findEnrolledCoursesByStudent(studentId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<CourseDTO.Response> getCoursesByLecturer(Long lecturerId) {
        return courseRepository.findByLecturerId(lecturerId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CourseDTO.Response updateCourse(Long id, CourseDTO.Request req) {
        Course course = findById(id);

        if (!course.getCourseCode().equals(req.getCourseCode())
                && courseRepository.existsByCourseCode(req.getCourseCode())) {
            throw new DuplicateResourceException("Course code " + req.getCourseCode() + " already exists");
        }

        course.setCourseCode(req.getCourseCode());
        course.setTitle(req.getTitle());
        course.setDescription(req.getDescription());
        course.setCreditHours(req.getCreditHours());
        course.setLevel(req.getLevel());
        course.setSemester(req.getSemester());

        if (req.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            course.setDepartment(dept);
        } else {
            course.setDepartment(null);
        }

        if (req.getLecturerId() != null) {
            Lecturer lecturer = lecturerRepository.findById(req.getLecturerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
            course.setLecturer(lecturer);
        } else {
            course.setLecturer(null);
        }

        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseDTO.Response assignLecturer(Long courseId, Long lecturerId) {
        Course course = findById(courseId);
        Lecturer lecturer = lecturerRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException("Lecturer not found"));
        course.setLecturer(lecturer);
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = findById(id);
        timetableRepository.deleteByCourseId(course.getId());
        courseRepository.delete(course);
    }

    private Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    private CourseDTO.Response toResponse(Course c) {
        return CourseDTO.Response.builder()
                .id(c.getId())
                .courseCode(c.getCourseCode())
                .title(c.getTitle())
                .description(c.getDescription())
                .creditHours(c.getCreditHours())
                .level(c.getLevel())
                .semester(c.getSemester())
                .status(c.getStatus())
                .departmentId(c.getDepartment() != null ? c.getDepartment().getId() : null)
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                .lecturerId(c.getLecturer() != null ? c.getLecturer().getId() : null)
                .lecturerName(c.getLecturer() != null ? c.getLecturer().getFullName() : null)
                .enrollmentCount(enrollmentRepository.countByCourseId(c.getId()))
                .build();
    }
}
