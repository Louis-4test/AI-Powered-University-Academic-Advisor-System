package com.pecar.academic.ai.controller;

import com.pecar.academic.ai.dto.ResearchAssistantDTO;
import com.pecar.academic.ai.service.ResearchAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai/research-assistant")
@RequiredArgsConstructor
public class ResearchAssistantController {

    private final ResearchAssistantService researchAssistantService;

    @PostMapping(value = "/analyze", consumes = "multipart/form-data")
    public ResponseEntity<ResearchAssistantDTO.Response> analyze(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(researchAssistantService.analyze(file));
    }
}
