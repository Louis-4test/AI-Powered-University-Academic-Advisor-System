package com.pecar.academic.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class CareerRecommendationDTO {

    @Data
    public static class Request {
        @NotNull(message = "Student id is required")
        private Long studentId;

        /** Optional free-text list of skills/interests the student wants considered
         *  beyond what's inferable from their courses and grades. */
        private List<String> additionalSkills;
    }

    public record CareerSuggestion(
            String careerTitle,
            int fitScore,           // 0-100
            String rationale,
            List<String> suggestedNextSteps
    ) {}

    /** Structured shape requested from the LLM via ChatClient.entity(). */
    public record AiRecommendation(List<CareerSuggestion> careers) {}

    public record Response(
            Long studentId,
            String studentName,
            List<CareerSuggestion> careers
    ) {}
}
