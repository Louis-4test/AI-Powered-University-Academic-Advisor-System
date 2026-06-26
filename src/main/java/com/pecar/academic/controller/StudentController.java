package com.pecar.academic.controller;

import com.pecar.academic.dto.StudentDTO;
import com.pecar.academic.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentDTO.Response> create(@Valid @RequestBody StudentDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    @GetMapping("/me")
    public ResponseEntity<StudentDTO.Response> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(studentService.getStudentByEmail(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/by-student-id/{studentId}")
    public ResponseEntity<StudentDTO.Response> getByStudentId(@PathVariable String studentId) {
        return ResponseEntity.ok(studentService.getStudentByStudentId(studentId));
    }

    @GetMapping
    public ResponseEntity<List<StudentDTO.Summary>> getAll() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/search")
    public ResponseEntity<List<StudentDTO.Summary>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(studentService.searchStudents(keyword));
    }

    @GetMapping("/high-achievers")
    public ResponseEntity<List<StudentDTO.Summary>> highAchievers(
            @RequestParam(defaultValue = "3.5") Double minCgpa) {
        return ResponseEntity.ok(studentService.getHighAchievers(minCgpa));
    }

    @GetMapping("/stats/by-level")
    public ResponseEntity<Map<Integer, Long>> statsByLevel() {
        return ResponseEntity.ok(studentService.getStudentCountByLevel());
    }

    @GetMapping("/stats/by-status")
    public ResponseEntity<Map<String, Long>> statsByStatus() {
        return ResponseEntity.ok(studentService.getStatusDistribution());
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDTO.Response> update(
            @PathVariable Long id, @Valid @RequestBody StudentDTO.UpdateRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
