package com.pecar.academic.ai.controller;

import com.pecar.academic.ai.dto.ChatbotDTO;
import com.pecar.academic.ai.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ChatbotDTO.Response> ask(@Valid @RequestBody ChatbotDTO.Request request) {
        return ResponseEntity.ok(chatbotService.ask(request));
    }
}
