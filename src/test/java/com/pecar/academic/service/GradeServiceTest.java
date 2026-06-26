package com.pecar.academic.service;

import com.pecar.academic.dto.GradeDTO;
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
class GradeServiceTest {

    @Mock private GradeRepository gradeRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;

    @InjectMocks private GradeService gradeService;

    private Student student;
    private Course course;
    private Grade grade;
    private GradeDTO.Request gradeReq;

    @BeforeEach
    void setUp() {
        student = Student.builder().id(1L).firstName("John").lastName("Doe").studentId("STU-001").build();
        course = Course.builder().id(1L).courseCode("CS101").title("Intro").build();

        grade = Grade.builder()
                .id(1L).student(student).course(course)
                .academicYear("2025/2026").semester("FIRST")
                .attendance(90.0).assignments(85.0).projects(80.0)
                .tests(75.0).exams(88.0)
                .build();
        grade.computeGrade();

        gradeReq = new GradeDTO.Request();
        gradeReq.setStudentId(1L);
        gradeReq.setCourseId(1L);
        gradeReq.setAcademicYear("2025/2026");
        gradeReq.setSemester("FIRST");
        gradeReq.setAttendance(95.0);
        gradeReq.setAssignments(90.0);
        gradeReq.setProjects(92.0);
        gradeReq.setTests(88.0);
        gradeReq.setExams(93.0);
    }

    @Test
    void recordGrade_success() {
        when(gradeRepository.findByStudentIdAndCourseIdAndAcademicYearAndSemester(anyLong(), anyLong(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(gradeRepository.save(any())).thenAnswer(i -> {
            Grade g = i.getArgument(0);
            g.setId(2L);
            return g;
        });

        GradeDTO.Response result = gradeService.recordGrade(gradeReq);
        assertNotNull(result);
        assertEquals("John Doe", result.getStudentName());
        assertEquals("CS101", result.getCourseCode());
    }

    @Test
    void recordGrade_duplicate() {
        when(gradeRepository.findByStudentIdAndCourseIdAndAcademicYearAndSemester(anyLong(), anyLong(), anyString(), anyString()))
                .thenReturn(Optional.of(grade));
        assertThrows(DuplicateResourceException.class, () -> gradeService.recordGrade(gradeReq));
    }

    @Test
    void updateGrade_success() {
        when(gradeRepository.findById(1L)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        GradeDTO.Request updateReq = new GradeDTO.Request();
        updateReq.setAttendance(100.0);
        updateReq.setAssignments(100.0);
        updateReq.setProjects(100.0);
        updateReq.setTests(100.0);
        updateReq.setExams(100.0);

        GradeDTO.Response result = gradeService.updateGrade(1L, updateReq);
        assertNotNull(result);
        assertEquals(100.0, result.getAttendance());
    }

    @Test
    void calculateCGPA() {
        when(gradeRepository.calculateCGPA(1L)).thenReturn(Optional.of(3.5));
        Double cgpa = gradeService.calculateCGPA(1L);
        assertEquals(3.5, cgpa);
    }

    @Test
    void calculateCGPA_noGrades() {
        when(gradeRepository.calculateCGPA(1L)).thenReturn(Optional.empty());
        Double cgpa = gradeService.calculateCGPA(1L);
        assertEquals(0.0, cgpa);
    }

    @Test
    void getPerformanceTrend() {
        Grade g1 = Grade.builder().student(student).course(course)
                .academicYear("2024/2025").semester("FIRST").gradePoint(3.0).build();
        Grade g2 = Grade.builder().student(student).course(course)
                .academicYear("2025/2026").semester("FIRST").gradePoint(3.5).build();
        when(gradeRepository.findByStudentId(1L)).thenReturn(List.of(g1, g2));

        List<Map<String, Object>> trend = gradeService.getPerformanceTrend(1L);
        assertEquals(2, trend.size());
    }

    @Test
    void getClassRanking() {
        Student s2 = Student.builder().id(2L).firstName("Jane").lastName("Doe").studentId("STU-002").build();
        when(studentRepository.findAll()).thenReturn(List.of(student, s2));
        when(gradeRepository.calculateCGPA(1L)).thenReturn(Optional.of(3.8));
        when(gradeRepository.calculateCGPA(2L)).thenReturn(Optional.of(3.2));

        List<Map<String, Object>> ranking = gradeService.getClassRanking();
        assertEquals(2, ranking.size());
        assertEquals(3.8, (Double) ranking.get(0).get("cgpa"), 0.01);
    }
}
