package com.pecar.academic.service;

import com.pecar.academic.dto.EnrollmentDTO;
import com.pecar.academic.entity.*;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;

    @InjectMocks private EnrollmentService enrollmentService;

    private Student student;
    private Course course;
    private Enrollment enrollment;
    private EnrollmentDTO.Request enrollReq;

    @BeforeEach
    void setUp() {
        student = Student.builder().id(1L).firstName("John").lastName("Doe").studentId("STU-001").build();
        course = Course.builder().id(1L).courseCode("CS101").title("Intro").build();

        enrollment = Enrollment.builder()
                .id(1L).student(student).course(course)
                .academicYear("2025/2026").semester("FIRST")
                .status(Enrollment.EnrollmentStatus.ENROLLED)
                .build();

        enrollReq = new EnrollmentDTO.Request();
        enrollReq.setStudentId(1L);
        enrollReq.setCourseId(1L);
        enrollReq.setAcademicYear("2025/2026");
        enrollReq.setSemester("FIRST");
    }

    @Test
    void enroll_success() {
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicYearAndSemester(anyLong(), anyLong(), anyString(), anyString()))
                .thenReturn(false);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EnrollmentDTO.Response result = enrollmentService.enroll(enrollReq);
        assertNotNull(result);
        assertEquals("John Doe", result.getStudentName());
        assertEquals("CS101", result.getCourseCode());
        assertEquals(Enrollment.EnrollmentStatus.ENROLLED, result.getStatus());
    }

    @Test
    void enroll_duplicate() {
        when(enrollmentRepository.existsByStudentIdAndCourseIdAndAcademicYearAndSemester(anyLong(), anyLong(), anyString(), anyString()))
                .thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> enrollmentService.enroll(enrollReq));
    }

    @Test
    void drop_success() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EnrollmentDTO.Response result = enrollmentService.drop(1L);
        assertEquals(Enrollment.EnrollmentStatus.DROPPED, result.getStatus());
    }

    @Test
    void complete_success() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(enrollmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        EnrollmentDTO.Response result = enrollmentService.complete(1L);
        assertEquals(Enrollment.EnrollmentStatus.COMPLETED, result.getStatus());
    }

    @Test
    void drop_notFound() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> enrollmentService.drop(99L));
    }

    @Test
    void getByStudent() {
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment));
        List<EnrollmentDTO.Response> results = enrollmentService.getByStudent(1L);
        assertEquals(1, results.size());
    }
}
