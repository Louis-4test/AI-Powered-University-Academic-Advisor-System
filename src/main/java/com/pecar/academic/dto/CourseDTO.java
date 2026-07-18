package com.pecar.academic.dto;

import com.pecar.academic.entity.Course;
import com.pecar.academic.entity.StudentLevel;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

public class CourseDTO {

    @Data
    public static class Request {
        @NotBlank
        private String courseCode;

        @NotBlank
        private String title;

        private String description;

        @NotNull @Min(1) @Max(6)
        private Integer creditHours;

        private StudentLevel level;
        private String semester;
        private Long departmentId;
        private Long lecturerId;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String courseCode;
        private String title;
        private String description;
        private Integer creditHours;
        private StudentLevel level;
        private String semester;
        private Course.CourseStatus status;
        private Long departmentId;
        private String departmentName;
        private Long lecturerId;
        private String lecturerName;
        private Long enrollmentCount;
    }
}
