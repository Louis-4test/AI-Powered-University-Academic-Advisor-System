package com.pecar.academic.service;

import com.pecar.academic.dto.DepartmentDTO;
import com.pecar.academic.entity.Department;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    public DepartmentDTO.Response create(DepartmentDTO.Request req) {
        if (departmentRepository.existsByCode(req.getCode())) {
            throw new DuplicateResourceException("Department code " + req.getCode() + " already exists");
        }
        if (departmentRepository.existsByName(req.getName())) {
            throw new DuplicateResourceException("Department name " + req.getName() + " already exists");
        }

        Department dept = Department.builder()
                .name(req.getName())
                .code(req.getCode())
                .description(req.getDescription())
                .build();

        return toResponse(departmentRepository.save(dept));
    }

    public DepartmentDTO.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public List<DepartmentDTO.Response> getAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public DepartmentDTO.Response update(Long id, DepartmentDTO.Request req) {
        Department dept = findById(id);

        if (!dept.getCode().equals(req.getCode()) && departmentRepository.existsByCode(req.getCode())) {
            throw new DuplicateResourceException("Department code " + req.getCode() + " already exists");
        }
        if (!dept.getName().equals(req.getName()) && departmentRepository.existsByName(req.getName())) {
            throw new DuplicateResourceException("Department name " + req.getName() + " already exists");
        }

        dept.setName(req.getName());
        dept.setCode(req.getCode());
        dept.setDescription(req.getDescription());

        return toResponse(departmentRepository.save(dept));
    }

    @Transactional
    public void delete(Long id) {
        Department dept = findById(id);

        // Deleting a department must never delete the people/courses in it —
        // unassign them first so the FK constraint doesn't block the delete
        // and so no student/lecturer/course record is lost.
        dept.getStudents().forEach(s -> s.setDepartment(null));
        dept.getLecturers().forEach(l -> l.setDepartment(null));
        dept.getCourses().forEach(c -> c.setDepartment(null));

        departmentRepository.delete(dept);
    }

    private Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    private DepartmentDTO.Response toResponse(Department d) {
        return DepartmentDTO.Response.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .description(d.getDescription())
                .studentCount(d.getStudents().size())
                .lecturerCount(d.getLecturers().size())
                .courseCount(d.getCourses().size())
                .build();
    }
}
