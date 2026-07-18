package com.pecar.academic.service;

import com.pecar.academic.dto.CourseDTO;
import com.pecar.academic.entity.*;
import com.pecar.academic.entity.StudentLevel;
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
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private LecturerRepository lecturerRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private TimetableRepository timetableRepository;

    @InjectMocks private CourseService courseService;

    private Department dept;
    private Lecturer lecturer;
    private Course course;
    private CourseDTO.Request req;

    @BeforeEach
    void setUp() {
        dept = Department.builder().id(1L).name("CS").code("CS").build();
        lecturer = Lecturer.builder().id(1L).firstName("John").lastName("Doe").build();

        course = Course.builder()
                .id(1L).courseCode("CS101").title("Intro to CS")
                .creditHours(4).level(StudentLevel.HND1).semester("FIRST")
                .department(dept).lecturer(lecturer)
                .build();

        req = new CourseDTO.Request();
        req.setCourseCode("CS201");
        req.setTitle("Data Structures");
        req.setCreditHours(4);
        req.setLevel(StudentLevel.HND2);
        req.setSemester("FIRST");
        req.setDepartmentId(1L);
        req.setLecturerId(1L);
    }

    @Test
    void createCourse_success() {
        when(courseRepository.existsByCourseCode("CS201")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(lecturerRepository.findById(1L)).thenReturn(Optional.of(lecturer));
        when(courseRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CourseDTO.Response result = courseService.createCourse(req);
        assertNotNull(result);
        assertEquals("CS201", result.getCourseCode());
        assertEquals("Data Structures", result.getTitle());
    }

    @Test
    void createCourse_duplicateCode() {
        when(courseRepository.existsByCourseCode("CS201")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> courseService.createCourse(req));
    }

    @Test
    void getCourseById_found() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(5L);

        CourseDTO.Response result = courseService.getCourseById(1L);
        assertNotNull(result);
        assertEquals("CS101", result.getCourseCode());
        assertEquals(5L, result.getEnrollmentCount());
    }

    @Test
    void getCourseById_notFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> courseService.getCourseById(99L));
    }

    @Test
    void assignLecturer_success() {
        Lecturer newLecturer = Lecturer.builder().id(2L).firstName("Jane").lastName("Smith").build();
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lecturerRepository.findById(2L)).thenReturn(Optional.of(newLecturer));
        when(courseRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(0L);

        CourseDTO.Response result = courseService.assignLecturer(1L, 2L);
        assertNotNull(result);
        assertEquals("Jane Smith", result.getLecturerName());
    }

    @Test
    void deleteCourse_success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        courseService.deleteCourse(1L);
        verify(courseRepository).delete(course);
    }
}
