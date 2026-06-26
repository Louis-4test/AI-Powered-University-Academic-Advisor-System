package com.pecar.academic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

public class DepartmentDTO {

    @Data
    public static class Request {
        @NotBlank private String name;
        @NotBlank private String code;
        private String description;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String code;
        private String description;
        private long studentCount;
        private long lecturerCount;
        private long courseCount;
    }
}
