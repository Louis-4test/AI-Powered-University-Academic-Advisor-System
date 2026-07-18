package com.pecar.academic.service;

import com.pecar.academic.dto.StudentDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private StudentService studentService;

    private Department dept;
    private Student student;
    private StudentDTO.Request createReq;
    private StudentDTO.UpdateRequest updateReq;

    @BeforeEach
    void setUp() {
        dept = Department.builder().id(1L).name("Computer Science").code("CS").build();

        student = Student.builder()
                .id(1L).studentId("STU-2025-001")
                .firstName("John").lastName("Doe").email("john@test.com")
                .currentLevel(StudentLevel.B_TECH).status(Student.StudentStatus.ACTIVE)
                .department(dept)
                .build();

        createReq = new StudentDTO.Request();
        createReq.setFirstName("Jane");
        createReq.setLastName("Smith");
        createReq.setEmail("jane@test.com");
        createReq.setPassword("pass123");
        createReq.setEnrollmentYear(2025);
        createReq.setCurrentLevel(StudentLevel.HND2);
        createReq.setDepartmentId(1L);

        updateReq = new StudentDTO.UpdateRequest();
        updateReq.setFirstName("John");
        updateReq.setLastName("Doe Updated");
        updateReq.setEmail("john.updated@test.com");
        updateReq.setCurrentLevel(StudentLevel.M_TECH1);
    }

    @Test
    void createStudent_success() {
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(studentRepository.count()).thenReturn(0L);
        when(studentRepository.save(any())).thenAnswer(i -> {
            Student s = i.getArgument(0);
            s.setId(1L);
            return s;
        });
        when(gradeRepository.calculateCGPA(anyLong())).thenReturn(Optional.of(0.0));

        StudentDTO.Response result = studentService.createStudent(createReq);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertEquals("jane@test.com", result.getEmail());
        verify(studentRepository).save(any());
        verify(userRepository).save(any());
    }

    @Test
    void createStudent_duplicateEmail() {
        when(studentRepository.existsByEmail("jane@test.com")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(createReq));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void getStudentById_found() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(gradeRepository.calculateCGPA(1L)).thenReturn(Optional.of(3.5));

        StudentDTO.Response result = studentService.getStudentById(1L);
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertEquals(3.5, result.getCgpa());
    }

    @Test
    void getStudentById_notFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(99L));
    }

    @Test
    void getAllStudents() {
        when(studentRepository.findAll()).thenReturn(List.of(student));
        when(gradeRepository.calculateCGPA(1L)).thenReturn(Optional.of(3.2));

        List<StudentDTO.Summary> result = studentService.getAllStudents();
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
    }

    @Test
    void updateStudent_success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(gradeRepository.calculateCGPA(1L)).thenReturn(Optional.of(3.0));

        StudentDTO.Response result = studentService.updateStudent(1L, updateReq);
        assertEquals("Doe Updated", result.getLastName());
    }

    @Test
    void deleteStudent_success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        studentService.deleteStudent(1L);
        verify(studentRepository).delete(student);
    }

    @Test
    void getHighAchievers() {
        Student s2 = Student.builder().id(2L).firstName("Low").lastName("Performer").build();
        when(studentRepository.findAll()).thenReturn(List.of(student, s2));
        when(gradeRepository.calculateCGPA(1L)).thenReturn(Optional.of(3.8));
        when(gradeRepository.calculateCGPA(2L)).thenReturn(Optional.of(2.0));

        List<StudentDTO.Summary> result = studentService.getHighAchievers(3.5);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
    }

    @Test
    void getStudentCountByLevel() {
        Student s2 = Student.builder().id(2L).currentLevel(StudentLevel.B_TECH).build();
        Student s3 = Student.builder().id(3L).currentLevel(StudentLevel.HND2).build();
        when(studentRepository.findAll()).thenReturn(List.of(student, s2, s3));

        Map<String, Long> result = studentService.getStudentCountByLevel();
        assertEquals(2, result.size());
        assertEquals(2L, result.get("B-TECH"));
        assertEquals(1L, result.get("HND2"));
    }
}
