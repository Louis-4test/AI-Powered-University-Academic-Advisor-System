package com.pecar.academic.ai.dto;

import lombok.Builder;
import lombok.Data;

public class ResearchAssistantDTO {

    public record AiAnalysis(
            String summary,
            String keyFindings,
            String researchGaps,
            String futureWork
    ) {}

    @Data
    @Builder
    public static class Response {
        private String fileName;
        private int extractedCharacterCount;
        private String summary;
        private String keyFindings;
        private String researchGaps;
        private String futureWork;
    }
}
