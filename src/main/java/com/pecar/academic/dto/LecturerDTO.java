package com.pecar.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

public class LecturerDTO {

    @Data
    public static class Request {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @NotBlank @Email private String email;
        private String phone;
        private String qualification;
        private String specialization;
        private Long departmentId;

        @NotBlank
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }

    @Data
    public static class UpdateRequest {
        @NotBlank private String firstName;
        @NotBlank private String lastName;
        @NotBlank @Email private String email;
        private String phone;
        private String qualification;
        private String specialization;
        private Long departmentId;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String lecturerId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private String qualification;
        private String specialization;
        private Long departmentId;
        private String departmentName;
        private int courseCount;
    }
}
