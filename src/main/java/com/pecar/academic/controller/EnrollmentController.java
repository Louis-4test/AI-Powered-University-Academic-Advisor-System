package com.pecar.academic.controller;

import com.pecar.academic.dto.EnrollmentDTO;
import com.pecar.academic.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<EnrollmentDTO.Response> enroll(@Valid @RequestBody EnrollmentDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(request));
    }

    @PatchMapping("/{id}/drop")
    public ResponseEntity<EnrollmentDTO.Response> drop(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.drop(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<EnrollmentDTO.Response> complete(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.complete(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<EnrollmentDTO.Response>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getByStudent(studentId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentDTO.Response>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getByCourse(courseId));
    }
}
