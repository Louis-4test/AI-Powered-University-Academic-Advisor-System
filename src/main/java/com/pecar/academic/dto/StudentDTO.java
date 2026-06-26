package com.pecar.academic.dto;

import com.pecar.academic.entity.Student;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudentDTO {

    // ── Request (create) ──────────────────────────────────────────────────────
    @Data
    public static class Request {

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        private String email;

        private String phone;
        private LocalDate dateOfBirth;

        @NotNull(message = "Enrollment year is required")
        private Integer enrollmentYear;

        @NotNull(message = "Current level is required")
        @Min(100) @Max(900)
        private Integer currentLevel;

        private String programName;
        private Long departmentId;

        // For creating account alongside student
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }

    // ── Request (update — no password; editing a student never touches login credentials) ──
    @Data
    public static class UpdateRequest {

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email")
        private String email;

        private String phone;
        private LocalDate dateOfBirth;

        @NotNull(message = "Current level is required")
        @Min(100) @Max(900)
        private Integer currentLevel;

        private String programName;
        private Long departmentId;
        private Student.StudentStatus status;
    }

    // ── Response ───────────────────────────────────────────────────────────────
    @Data
    @Builder
    public static class Response {
        private Long id;
        private String studentId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private LocalDate dateOfBirth;
        private Integer enrollmentYear;
        private Integer currentLevel;
        private String programName;
        private Student.StudentStatus status;
        private Long departmentId;
        private String departmentName;
        private Double cgpa;
        private LocalDateTime createdAt;
    }

    // ── Summary (for lists) ────────────────────────────────────────────────────
    @Data
    @Builder
    public static class Summary {
        private Long id;
        private String studentId;
        private String fullName;
        private String email;
        private Integer currentLevel;
        private Student.StudentStatus status;
        private Double cgpa;
    }
}
