package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.CareerRecommendationDTO;
import com.pecar.academic.ai.dto.CareerRecommendationDTO.AiRecommendation;
import com.pecar.academic.entity.Enrollment;
import com.pecar.academic.entity.Grade;
import com.pecar.academic.entity.Student;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.EnrollmentRepository;
import com.pecar.academic.repository.GradeRepository;
import com.pecar.academic.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI Module 5: Career Recommendation Engine.
 *
 * Builds the student's real profile (completed/enrolled courses, grades, CGPA)
 * from the database, optionally combines it with student-supplied skills, and
 * asks the model to recommend a ranked list of career paths with rationale.
 */
@Service
@RequiredArgsConstructor
public class CareerRecommendationService {

    private final StudentRepository    studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository      gradeRepository;
    private final ChatClient           chatClient;

    public CareerRecommendationDTO.Response recommend(CareerRecommendationDTO.Request request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        String profile = buildProfile(student, request.getAdditionalSkills());

        AiRecommendation recommendation = chatClient.prompt()
                .user(u -> u.text("""
                        Based on the following verified student academic profile, recommend 3 to 5 career
                        paths that best fit this student (e.g. Cybersecurity Analyst, Cloud Engineer, Data
                        Scientist, AI Engineer, Software Architect, or other relevant titles — you are not
                        limited to this list).

                        For each recommendation provide:
                        - careerTitle
                        - fitScore (0-100, how strongly this student's profile matches the role)
                        - rationale (2-3 sentences referencing specific courses, grades, or skills from the
                          profile below)
                        - suggestedNextSteps (2-4 concrete actions, e.g. specific courses to take, projects
                          to build, certifications to pursue)

                        Order the list from highest to lowest fitScore.

                        Student profile:
                        {profile}
                        """)
                        .param("profile", profile))
                .call()
                .entity(AiRecommendation.class);

        return new CareerRecommendationDTO.Response(
                student.getId(),
                student.getFullName(),
                recommendation.careers()
        );
    }

    // ── Profile construction from real data ─────────────────────────────────────

    private String buildProfile(Student student, List<String> additionalSkills) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        List<Grade> grades = gradeRepository.findByStudentId(student.getId());

        String completedCourses = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED
                          || e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .map(e -> e.getCourse().getCourseCode() + " (" + e.getCourse().getTitle() + ")")
                .distinct()
                .collect(Collectors.joining(", "));

        String gradesSummary = grades.stream()
                .map(g -> g.getCourse().getCourseCode() + ": " + g.getLetterGrade()
                        + " (" + String.format("%.1f", g.getTotalScore()) + "/100)")
                .collect(Collectors.joining(", "));

        double cgpa = gradeRepository.calculateCGPA(student.getId()).orElse(0.0);

        String skills = (additionalSkills == null || additionalSkills.isEmpty())
                ? "None explicitly provided — infer likely skills from the courses and grades above"
                : String.join(", ", additionalSkills);

        return """
                Name: %s
                Program: %s
                Department: %s
                Current level: %d
                CGPA: %.2f
                Courses taken: %s
                Grades by course: %s
                Self-reported skills/interests: %s
                """.formatted(
                        student.getFullName(),
                        student.getProgramName(),
                        student.getDepartment() != null ? student.getDepartment().getName() : "Unassigned",
                        student.getCurrentLevel(),
                        cgpa,
                        completedCourses.isEmpty() ? "None recorded" : completedCourses,
                        gradesSummary.isEmpty() ? "None recorded" : gradesSummary,
                        skills
                );
    }
}
