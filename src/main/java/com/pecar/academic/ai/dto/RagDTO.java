package com.pecar.academic.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

public class RagDTO {

    @Data
    public static class IngestResponse {
        private String message;
        private String fileName;
        private int chunkCount;
    }

    @Data
    public static class QueryRequest {
        @NotBlank(message = "Question is required")
        private String question;
    }

    @Data
    @Builder
    public static class QueryResponse {
        private String answer;
        private int sourceCount;
    }
}
