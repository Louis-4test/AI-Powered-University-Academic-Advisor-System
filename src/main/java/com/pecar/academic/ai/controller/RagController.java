package com.pecar.academic.ai.controller;

import com.pecar.academic.ai.dto.RagDTO;
import com.pecar.academic.ai.service.RagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagService ragService;

    @PostMapping("/ingest")
    public ResponseEntity<RagDTO.IngestResponse> ingestDocument(@RequestParam MultipartFile file) {
        return ResponseEntity.ok(ragService.ingestDocument(file));
    }

    @PostMapping("/query")
    public ResponseEntity<RagDTO.QueryResponse> query(@Valid @RequestBody RagDTO.QueryRequest request) {
        return ResponseEntity.ok(ragService.query(request));
    }
}
