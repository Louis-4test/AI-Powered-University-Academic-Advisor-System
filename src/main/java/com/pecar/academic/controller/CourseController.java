package com.pecar.academic.controller;

import com.pecar.academic.dto.CourseDTO;
import com.pecar.academic.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseDTO.Response> create(@Valid @RequestBody CourseDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO.Response>> getAll() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO.Response>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(courseService.searchCourses(keyword));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<CourseDTO.Response>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.getCoursesByStudent(studentId));
    }

    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<List<CourseDTO.Response>> getByLecturer(@PathVariable Long lecturerId) {
        return ResponseEntity.ok(courseService.getCoursesByLecturer(lecturerId));
    }

    @PatchMapping("/{courseId}/assign-lecturer/{lecturerId}")
    public ResponseEntity<CourseDTO.Response> assignLecturer(
            @PathVariable Long courseId, @PathVariable Long lecturerId) {
        return ResponseEntity.ok(courseService.assignLecturer(courseId, lecturerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO.Response> update(
            @PathVariable Long id, @Valid @RequestBody CourseDTO.Request request) {
        return ResponseEntity.ok(courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
