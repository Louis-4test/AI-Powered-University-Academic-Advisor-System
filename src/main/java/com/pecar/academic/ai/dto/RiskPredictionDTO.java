package com.pecar.academic.ai.dto;

import lombok.Builder;
import lombok.Data;

public class RiskPredictionDTO {

    public enum RiskCategory {
        EXCELLENT_PERFORMANCE,
        LIKELY_TO_PASS,
        AT_RISK,
        LIKELY_TO_FAIL
    }

    /** Structured shape requested from the LLM via ChatClient.entity(). */
    public record AiAssessment(
            RiskCategory category,
            int riskScore,          // 0-100, higher = more risk of failing
            String explanation,
            String recommendation
    ) {}

    @Data
    @Builder
    public static class Response {
        private Long studentId;
        private String studentName;
        private String courseCode;
        private Double attendance;
        private Double assignments;
        private Double projects;
        private Double tests;
        private Double exams;
        private RiskCategory category;
        private int riskScore;
        private String explanation;
        private String recommendation;
    }
}
