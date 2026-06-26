package com.pecar.academic.service;

import com.pecar.academic.dto.DepartmentDTO;
import com.pecar.academic.entity.Department;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.DepartmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock private DepartmentRepository departmentRepository;

    @InjectMocks private DepartmentService departmentService;

    private Department dept;
    private DepartmentDTO.Request req;

    @BeforeEach
    void setUp() {
        dept = Department.builder().id(1L).name("Computer Science").code("CS").description("CS dept").build();

        req = new DepartmentDTO.Request();
        req.setName("Mathematics");
        req.setCode("MTH");
        req.setDescription("Math dept");
    }

    @Test
    void create_success() {
        when(departmentRepository.existsByCode("MTH")).thenReturn(false);
        when(departmentRepository.existsByName("Mathematics")).thenReturn(false);
        when(departmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        DepartmentDTO.Response result = departmentService.create(req);
        assertNotNull(result);
        assertEquals("Mathematics", result.getName());
        assertEquals("MTH", result.getCode());
    }

    @Test
    void create_duplicateCode() {
        when(departmentRepository.existsByCode("MTH")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> departmentService.create(req));
    }

    @Test
    void getById_found() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        DepartmentDTO.Response result = departmentService.getById(1L);
        assertNotNull(result);
        assertEquals("Computer Science", result.getName());
    }

    @Test
    void getById_notFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> departmentService.getById(99L));
    }

    @Test
    void getAll() {
        when(departmentRepository.findAll()).thenReturn(List.of(dept));
        List<DepartmentDTO.Response> result = departmentService.getAll();
        assertEquals(1, result.size());
    }

    @Test
    void update_success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(departmentRepository.existsByCode("MTH")).thenReturn(false);
        when(departmentRepository.existsByName("Mathematics")).thenReturn(false);
        when(departmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        DepartmentDTO.Response result = departmentService.update(1L, req);
        assertEquals("Mathematics", result.getName());
    }

    @Test
    void delete_success() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        departmentService.delete(1L);
        verify(departmentRepository).delete(dept);
    }
}
