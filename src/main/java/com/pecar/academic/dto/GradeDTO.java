package com.pecar.academic.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

public class GradeDTO {

    @Data
    public static class Request {
        @NotNull private Long studentId;
        @NotNull private Long courseId;
        @NotBlank private String academicYear;
        @NotBlank private String semester;

        @Min(0) @Max(100) private Double attendance;
        @Min(0) @Max(100) private Double assignments;
        @Min(0) @Max(100) private Double projects;
        @Min(0) @Max(100) private Double tests;
        @Min(0) @Max(100) private Double exams;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String studentName;
        private String courseCode;
        private String courseTitle;
        private String academicYear;
        private String semester;
        private Double attendance;
        private Double assignments;
        private Double projects;
        private Double tests;
        private Double exams;
        private Double totalScore;
        private String letterGrade;
        private Double gradePoint;
    }
}
