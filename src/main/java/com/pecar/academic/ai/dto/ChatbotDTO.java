package com.pecar.academic.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

public class ChatbotDTO {

    @Data
    public static class Request {
        @NotBlank(message = "Question is required")
        private String question;

        /** Optional: when provided, the assistant grounds graduation/progress
         *  questions in this student's real enrollment and grade data. */
        private Long studentId;
    }

    @Data
    @Builder
    public static class Response {
        private String answer;
    }
}
