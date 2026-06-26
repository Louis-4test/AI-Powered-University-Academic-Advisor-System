package com.pecar.academic.controller;

import com.pecar.academic.dto.LecturerDTO;
import com.pecar.academic.service.LecturerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturers")
@RequiredArgsConstructor
public class LecturerController {

    private final LecturerService lecturerService;

    @PostMapping
    public ResponseEntity<LecturerDTO.Response> create(@Valid @RequestBody LecturerDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lecturerService.create(request));
    }

    @GetMapping("/me")
    public ResponseEntity<LecturerDTO.Response> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(lecturerService.getByEmail(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LecturerDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(lecturerService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<LecturerDTO.Response>> getAll() {
        return ResponseEntity.ok(lecturerService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<LecturerDTO.Response>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(lecturerService.search(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LecturerDTO.Response> update(
            @PathVariable Long id, @Valid @RequestBody LecturerDTO.UpdateRequest request) {
        return ResponseEntity.ok(lecturerService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lecturerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
