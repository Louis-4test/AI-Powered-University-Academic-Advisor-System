package com.pecar.academic.ai.controller;

import com.pecar.academic.ai.dto.RiskPredictionDTO;
import com.pecar.academic.ai.service.RiskPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/risk-prediction")
@RequiredArgsConstructor
public class RiskPredictionController {

    private final RiskPredictionService riskPredictionService;

    @GetMapping("/grade/{gradeId}")
    public ResponseEntity<RiskPredictionDTO.Response> predict(@PathVariable Long gradeId) {
        return ResponseEntity.ok(riskPredictionService.predict(gradeId));
    }
}
