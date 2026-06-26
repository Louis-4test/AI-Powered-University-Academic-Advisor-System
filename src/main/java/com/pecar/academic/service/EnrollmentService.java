package com.pecar.academic.service;

import com.pecar.academic.dto.EnrollmentDTO;
import com.pecar.academic.entity.Course;
import com.pecar.academic.entity.Enrollment;
import com.pecar.academic.entity.Student;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.CourseRepository;
import com.pecar.academic.repository.EnrollmentRepository;
import com.pecar.academic.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository    studentRepository;
    private final CourseRepository     courseRepository;

    @Transactional
    public EnrollmentDTO.Response enroll(EnrollmentDTO.Request req) {
        boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicYearAndSemester(
                req.getStudentId(), req.getCourseId(), req.getAcademicYear(), req.getSemester());

        if (alreadyEnrolled) {
            throw new DuplicateResourceException("Student is already enrolled in this course for the given term");
        }

        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .academicYear(req.getAcademicYear())
                .semester(req.getSemester())
                .build();

        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentDTO.Response drop(Long enrollmentId) {
        Enrollment enrollment = findById(enrollmentId);
        enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
        return toResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentDTO.Response complete(Long enrollmentId) {
        Enrollment enrollment = findById(enrollmentId);
        enrollment.setStatus(Enrollment.EnrollmentStatus.COMPLETED);
        return toResponse(enrollmentRepository.save(enrollment));
    }

    public List<EnrollmentDTO.Response> getByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO.Response> getByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Enrollment findById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
    }

    private EnrollmentDTO.Response toResponse(Enrollment e) {
        return EnrollmentDTO.Response.builder()
                .id(e.getId())
                .studentName(e.getStudent().getFullName())
                .studentNumber(e.getStudent().getStudentId())
                .courseCode(e.getCourse().getCourseCode())
                .courseTitle(e.getCourse().getTitle())
                .academicYear(e.getAcademicYear())
                .semester(e.getSemester())
                .enrollmentDate(e.getEnrollmentDate())
                .status(e.getStatus())
                .build();
    }
}
