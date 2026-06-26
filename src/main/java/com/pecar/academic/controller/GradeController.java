package com.pecar.academic.controller;

import com.pecar.academic.dto.GradeDTO;
import com.pecar.academic.service.GradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping
    public ResponseEntity<GradeDTO.Response> recordGrade(@Valid @RequestBody GradeDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.recordGrade(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradeDTO.Response> updateGrade(
            @PathVariable Long id, @Valid @RequestBody GradeDTO.Request request) {
        return ResponseEntity.ok(gradeService.updateGrade(id, request));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<GradeDTO.Response>> getStudentGrades(@PathVariable Long studentId) {
        return ResponseEntity.ok(gradeService.getStudentGrades(studentId));
    }

    @GetMapping("/student/{studentId}/cgpa")
    public ResponseEntity<Map<String, Double>> getCgpa(@PathVariable Long studentId) {
        return ResponseEntity.ok(Map.of("cgpa", gradeService.calculateCGPA(studentId)));
    }

    @GetMapping("/student/{studentId}/gpa")
    public ResponseEntity<Map<String, Double>> getSemesterGpa(
            @PathVariable Long studentId,
            @RequestParam String academicYear,
            @RequestParam String semester) {
        Double gpa = gradeService.calculateSemesterGPA(studentId, academicYear, semester);
        return ResponseEntity.ok(Map.of("gpa", gpa));
    }

    @GetMapping("/student/{studentId}/trend")
    public ResponseEntity<List<Map<String, Object>>> getPerformanceTrend(@PathVariable Long studentId) {
        return ResponseEntity.ok(gradeService.getPerformanceTrend(studentId));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<Map<String, Object>>> getClassRanking() {
        return ResponseEntity.ok(gradeService.getClassRanking());
    }
}
