package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.RiskPredictionDTO;
import com.pecar.academic.ai.dto.RiskPredictionDTO.AiAssessment;
import com.pecar.academic.ai.dto.RiskPredictionDTO.RiskCategory;
import com.pecar.academic.entity.Grade;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AI Module 2: Student Risk Prediction.
 *
 * Inputs (attendance, assignments, projects, tests, exams) are real numeric
 * grade components already stored in the database. The risk CATEGORY and
 * SCORE are computed deterministically from these numbers (so the result is
 * reproducible and auditable, never hallucinated), while the LLM is used only
 * to generate a natural-language explanation and an actionable recommendation
 * for the student/lecturer.
 */
@Service
@RequiredArgsConstructor
public class RiskPredictionService {

    private final GradeRepository gradeRepository;
    private final ChatClient      chatClient;

    public RiskPredictionDTO.Response predict(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found"));

        int riskScore = computeRiskScore(grade);
        RiskCategory category = categorize(riskScore, grade.getGradePoint());

        AiAssessment assessment = generateAssessment(grade, category, riskScore);

        return RiskPredictionDTO.Response.builder()
                .studentId(grade.getStudent().getId())
                .studentName(grade.getStudent().getFullName())
                .courseCode(grade.getCourse().getCourseCode())
                .attendance(grade.getAttendance())
                .assignments(grade.getAssignments())
                .projects(grade.getProjects())
                .tests(grade.getTests())
                .exams(grade.getExams())
                .category(category)
                .riskScore(riskScore)
                .explanation(assessment.explanation())
                .recommendation(assessment.recommendation())
                .build();
    }

    // ── Deterministic scoring ───────────────────────────────────────────────────

    /**
     * Risk score 0-100: higher means MORE at risk of failing. Derived from the
     * same weighted formula used for grading (attendance 10%, assignments 15%,
     * projects 15%, tests 20%, exams 40%), inverted, with attendance weighted
     * slightly heavier here since low attendance is historically one of the
     * strongest leading indicators of failure.
     */
    private int computeRiskScore(Grade grade) {
        double performanceScore = (grade.getAttendance()  * 0.20)
                                 + (grade.getAssignments() * 0.15)
                                 + (grade.getProjects()    * 0.15)
                                 + (grade.getTests()       * 0.20)
                                 + (grade.getExams()       * 0.30);

        double risk = 100.0 - performanceScore;
        return (int) Math.round(Math.max(0, Math.min(100, risk)));
    }

    private RiskCategory categorize(int riskScore, Double gradePoint) {
        if (gradePoint != null && gradePoint >= 3.5 && riskScore < 20) {
            return RiskCategory.EXCELLENT_PERFORMANCE;
        }
        if (riskScore >= 60) {
            return RiskCategory.LIKELY_TO_FAIL;
        }
        if (riskScore >= 40) {
            return RiskCategory.AT_RISK;
        }
        return RiskCategory.LIKELY_TO_PASS;
    }

    // ── LLM-generated explanation ──────────────────────────────────────────────

    private AiAssessment generateAssessment(Grade grade, RiskCategory category, int riskScore) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        A student has the following verified academic performance in {course}:
                        Attendance: {attendance}/100
                        Assignments: {assignments}/100
                        Projects: {projects}/100
                        Tests: {tests}/100
                        Exams: {exams}/100

                        A deterministic scoring model has already classified this student as: {category}
                        with a computed risk score of {riskScore} out of 100 (higher = more at risk of failing).

                        Do not change the category or score — they are fixed. Your job is only to:
                        1. Write a brief (2-3 sentence) explanation of what is driving this result, referencing
                           the specific component scores above.
                        2. Write a brief (2-3 sentence) actionable recommendation for the student and their
                           academic advisor.
                        """)
                        .param("course", grade.getCourse().getTitle())
                        .param("attendance", grade.getAttendance())
                        .param("assignments", grade.getAssignments())
                        .param("projects", grade.getProjects())
                        .param("tests", grade.getTests())
                        .param("exams", grade.getExams())
                        .param("category", category.name())
                        .param("riskScore", riskScore))
                .call()
                .entity(AiAssessment.class);
    }
}
