package com.pecar.academic.controller;

import com.pecar.academic.dto.TimetableDTO;
import com.pecar.academic.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping
    public ResponseEntity<TimetableDTO.Response> create(@Valid @RequestBody TimetableDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(timetableService.createEntry(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimetableDTO.Response> update(
            @PathVariable Long id, @Valid @RequestBody TimetableDTO.Request request) {
        return ResponseEntity.ok(timetableService.updateEntry(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        timetableService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<TimetableDTO.Response>> getAll() {
        return ResponseEntity.ok(timetableService.getAllEntries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimetableDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(timetableService.getEntryById(id));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<TimetableDTO.Response>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(timetableService.getByDepartment(departmentId));
    }

    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<List<TimetableDTO.Response>> getByLecturer(@PathVariable Long lecturerId) {
        return ResponseEntity.ok(timetableService.getByLecturer(lecturerId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TimetableDTO.Response>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(timetableService.getByStudent(studentId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TimetableDTO.Response>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(timetableService.getByCourse(courseId));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<TimetableDTO.Response>> generate(
            @Valid @RequestBody TimetableDTO.GenerateRequest request) {
        return ResponseEntity.ok(timetableService.generateTimetable(request));
    }
}
