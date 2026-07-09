package com.pecar.academic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LecturerStudentDTO {
    private Long studentId;
    private String studentNumber;
    private String fullName;
    private String email;
    private Integer currentLevel;
    private String programName;
    private String departmentName;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private String academicYear;
    private String semester;
}
