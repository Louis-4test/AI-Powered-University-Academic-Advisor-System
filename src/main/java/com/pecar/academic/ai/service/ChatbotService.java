package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.ChatbotDTO;
import com.pecar.academic.entity.Course;
import com.pecar.academic.entity.Enrollment;
import com.pecar.academic.entity.Student;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.CourseRepository;
import com.pecar.academic.repository.EnrollmentRepository;
import com.pecar.academic.repository.GradeRepository;
import com.pecar.academic.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI Module 1: Academic Chatbot.
 *
 * Answers general academic questions (e.g. "What is polymorphism?",
 * "Explain database normalization", "Suggest a project topic in cybersecurity")
 * directly via the LLM. When a studentId is supplied and the question concerns
 * the student's own progress (e.g. "Which courses remain for graduation?"),
 * the student's real enrollment/grade data is injected into the prompt so the
 * model answers from facts rather than guessing.
 */
@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatClient        chatClient;
    private final StudentRepository studentRepository;
    private final CourseRepository  courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository   gradeRepository;

    public ChatbotDTO.Response ask(ChatbotDTO.Request request) {
        String context = request.getStudentId() != null
                ? buildStudentContext(request.getStudentId())
                : "";

        String answer = chatClient.prompt()
                .user(u -> u.text("""
                        {context}

                        Student question: {question}
                        """)
                        .param("context", context)
                        .param("question", request.getQuestion()))
                .call()
                .content();

        return ChatbotDTO.Response.builder().answer(answer).build();
    }

    // ── Grounding context for student-specific questions ───────────────────────

    private String buildStudentContext(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        Set<String> completedCourseCodes = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.COMPLETED)
                .map(e -> e.getCourse().getCourseCode())
                .collect(Collectors.toSet());

        Set<String> enrolledCourseCodes = enrollments.stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .map(e -> e.getCourse().getCourseCode())
                .collect(Collectors.toSet());

        List<Course> departmentCourses = student.getDepartment() != null
                ? courseRepository.findByDepartmentId(student.getDepartment().getId())
                : List.of();

        List<String> remainingCourses = departmentCourses.stream()
                .filter(c -> c.getLevel() <= student.getCurrentLevel())
                .filter(c -> !completedCourseCodes.contains(c.getCourseCode()))
                .filter(c -> !enrolledCourseCodes.contains(c.getCourseCode()))
                .map(c -> c.getCourseCode() + " - " + c.getTitle() + " (" + c.getCreditHours() + " credits)")
                .collect(Collectors.toList());

        double cgpa = gradeRepository.calculateCGPA(studentId).orElse(0.0);

        return """
                Use the following verified student record as ground truth — do not invent any
                additional courses, grades, or facts beyond what is listed here:

                Student: %s (%s)
                Program: %s
                Current level: %d
                Department: %s
                Current CGPA: %.2f
                Completed courses: %s
                Currently enrolled courses: %s
                Courses available in this department at or below the student's level that have
                NOT yet been completed or enrolled in: %s
                """.formatted(
                        student.getFullName(),
                        student.getStudentId(),
                        student.getProgramName(),
                        student.getCurrentLevel(),
                        student.getDepartment() != null ? student.getDepartment().getName() : "Unassigned",
                        cgpa,
                        completedCourseCodes.isEmpty() ? "None recorded" : String.join(", ", completedCourseCodes),
                        enrolledCourseCodes.isEmpty() ? "None" : String.join(", ", enrolledCourseCodes),
                        remainingCourses.isEmpty() ? "None — all matching courses accounted for" : String.join("; ", remainingCourses)
                );
    }
}
