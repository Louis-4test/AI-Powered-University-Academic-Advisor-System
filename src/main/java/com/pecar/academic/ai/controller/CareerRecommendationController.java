package com.pecar.academic.ai.controller;

import com.pecar.academic.ai.dto.CareerRecommendationDTO;
import com.pecar.academic.ai.service.CareerRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/career-recommendation")
@RequiredArgsConstructor
public class CareerRecommendationController {

    private final CareerRecommendationService careerRecommendationService;

    @PostMapping("/recommend")
    public ResponseEntity<CareerRecommendationDTO.Response> recommend(
            @Valid @RequestBody CareerRecommendationDTO.Request request) {
        return ResponseEntity.ok(careerRecommendationService.recommend(request));
    }
}
