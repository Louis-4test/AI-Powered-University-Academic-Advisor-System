package com.pecar.academic.ai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class ExamGeneratorDTO {

    public enum Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }

    @Data
    public static class Request {
        @NotBlank(message = "Topic is required")
        private String topic;

        @NotNull(message = "Difficulty is required")
        private Difficulty difficulty;

        @NotNull
        @Min(1) @Max(50)
        private Integer questionCount;

        /** Optional: restrict to specific question types; defaults to all four. */
        private List<String> questionTypes;
    }

    public record McqQuestion(String question, List<String> options, String correctAnswer, String explanation) {}
    public record TheoryQuestion(String question, String guidanceForGrading) {}
    public record PracticalQuestion(String question, String expectedApproach) {}
    public record CaseStudy(String scenario, String question) {}

    /** Structured shape requested from the LLM via ChatClient.entity(). */
    public record AiExam(
            List<McqQuestion> mcqs,
            List<TheoryQuestion> theoryQuestions,
            List<PracticalQuestion> practicalQuestions,
            List<CaseStudy> caseStudies
    ) {}

    public record Response(
            String topic,
            Difficulty difficulty,
            int requestedQuestionCount,
            List<McqQuestion> mcqs,
            List<TheoryQuestion> theoryQuestions,
            List<PracticalQuestion> practicalQuestions,
            List<CaseStudy> caseStudies
    ) {}
}
