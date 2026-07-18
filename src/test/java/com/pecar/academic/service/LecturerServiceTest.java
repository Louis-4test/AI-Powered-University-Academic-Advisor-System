package com.pecar.academic.service;

import com.pecar.academic.dto.LecturerDTO;
import com.pecar.academic.entity.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LecturerServiceTest {

    @Mock private LecturerRepository lecturerRepository;
    @Mock private UserRepository userRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private TimetableRepository timetableRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private LecturerService lecturerService;

    private Department dept;
    private Lecturer lecturer;

    @BeforeEach
    void setUp() {
        dept = Department.builder().id(1L).name("CS").code("CS").build();
        lecturer = Lecturer.builder()
                .id(1L).lecturerId("LEC-2025-001")
                .firstName("John").lastName("Doe")
                .email("john@pecar.edu")
                .department(dept)
                .courses(new ArrayList<>())
                .build();
    }

    @Test
    void create_success() {
        LecturerDTO.Request req = new LecturerDTO.Request();
        req.setFirstName("Jane");
        req.setLastName("Smith");
        req.setEmail("jane@pecar.edu");
        req.setPassword("pass123");
        req.setDepartmentId(1L);

        when(lecturerRepository.existsByEmail("jane@pecar.edu")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(lecturerRepository.count()).thenReturn(0L);
        when(lecturerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LecturerDTO.Response result = lecturerService.create(req);
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void getById_found() {
        when(lecturerRepository.findById(1L)).thenReturn(Optional.of(lecturer));
        LecturerDTO.Response result = lecturerService.getById(1L);
        assertEquals("John Doe", result.getFullName());
    }

    @Test
    void getById_notFound() {
        when(lecturerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> lecturerService.getById(99L));
    }

    @Test
    void delete_success() {
        when(lecturerRepository.findById(1L)).thenReturn(Optional.of(lecturer));
        lecturerService.delete(1L);
        verify(lecturerRepository).delete(lecturer);
    }
}
