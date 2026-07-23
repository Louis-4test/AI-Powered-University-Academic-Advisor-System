package com.pecar.academic.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pecar.academic.entity.DayOfWeek;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

public class TimetableDTO {

    @Data
    public static class Request {
        @NotNull private Long courseId;
        private Long lecturerId;

        @NotNull private DayOfWeek dayOfWeek;

        @NotNull private String startTime;
        @NotNull private String endTime;

        private String room;
        private String location;
        private Long departmentId;
        private String academicYear;
        private String semester;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private Long courseId;
        private String courseCode;
        private String courseTitle;
        private Long lecturerId;
        private String lecturerName;
        private DayOfWeek dayOfWeek;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;
        private String room;
        private String location;
        private Long departmentId;
        private String departmentName;
        private String academicYear;
        private String semester;
    }

    @Data
    @Builder
    public static class GenerateRequest {
        @NotNull private Long departmentId;
        @NotBlank private String academicYear;
        @NotBlank private String semester;
    }
}
