package com.pecar.academic.dto;

import com.pecar.academic.entity.Enrollment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

public class EnrollmentDTO {

    @Data
    public static class Request {
        @NotNull private Long studentId;
        @NotNull private Long courseId;
        @NotBlank private String academicYear;
        @NotBlank private String semester;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String studentName;
        private String studentNumber;
        private String courseCode;
        private String courseTitle;
        private String academicYear;
        private String semester;
        private LocalDate enrollmentDate;
        private Enrollment.EnrollmentStatus status;
    }
}
