package com.pecar.academic.ai.controller;

import com.pecar.academic.ai.dto.ExamGeneratorDTO;
import com.pecar.academic.ai.service.ExamGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/exam-generator")
@RequiredArgsConstructor
public class ExamGeneratorController {

    private final ExamGeneratorService examGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<ExamGeneratorDTO.Response> generate(@Valid @RequestBody ExamGeneratorDTO.Request request) {
        return ResponseEntity.ok(examGeneratorService.generate(request));
    }
}
