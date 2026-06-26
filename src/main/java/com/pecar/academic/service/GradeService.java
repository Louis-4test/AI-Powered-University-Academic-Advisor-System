package com.pecar.academic.service;

import com.pecar.academic.dto.GradeDTO;
import com.pecar.academic.entity.Course;
import com.pecar.academic.entity.Grade;
import com.pecar.academic.entity.Student;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.CourseRepository;
import com.pecar.academic.repository.GradeRepository;
import com.pecar.academic.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRepository   gradeRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository  courseRepository;

    // ── Record a grade ─────────────────────────────────────────────────────────

    @Transactional
    public GradeDTO.Response recordGrade(GradeDTO.Request req) {
        boolean exists = gradeRepository.findByStudentIdAndCourseIdAndAcademicYearAndSemester(
                req.getStudentId(), req.getCourseId(), req.getAcademicYear(), req.getSemester()).isPresent();

        if (exists) {
            throw new DuplicateResourceException("Grade already recorded for this student/course/semester");
        }

        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Grade grade = Grade.builder()
                .student(student)
                .course(course)
                .academicYear(req.getAcademicYear())
                .semester(req.getSemester())
                .attendance(req.getAttendance())
                .assignments(req.getAssignments())
                .projects(req.getProjects())
                .tests(req.getTests())
                .exams(req.getExams())
                .build();

        return toResponse(gradeRepository.save(grade));
    }

    // ── Update grade ───────────────────────────────────────────────────────────

    @Transactional
    public GradeDTO.Response updateGrade(Long gradeId, GradeDTO.Request req) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

        grade.setAttendance(req.getAttendance());
        grade.setAssignments(req.getAssignments());
        grade.setProjects(req.getProjects());
        grade.setTests(req.getTests());
        grade.setExams(req.getExams());

        return toResponse(gradeRepository.save(grade));
    }

    // ── GPA / CGPA ─────────────────────────────────────────────────────────────

    public Double calculateCGPA(Long studentId) {
        return gradeRepository.calculateCGPA(studentId).orElse(0.0);
    }

    public Double calculateSemesterGPA(Long studentId, String year, String semester) {
        return gradeRepository.calculateSemesterGPA(studentId, year, semester).orElse(0.0);
    }

    // ── Class ranking using Streams ────────────────────────────────────────────

    public List<Map<String, Object>> getClassRanking() {
        List<Student> allStudents = studentRepository.findAll();

        return allStudents.stream()
                .map(s -> {
                    double cgpa = gradeRepository.calculateCGPA(s.getId()).orElse(0.0);
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("studentId", s.getStudentId());
                    entry.put("fullName",  s.getFullName());
                    entry.put("cgpa",      Math.round(cgpa * 100.0) / 100.0);
                    return entry;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("cgpa"), (Double) a.get("cgpa")))
                .collect(Collectors.toList());
    }

    // ── Performance trends using Streams ──────────────────────────────────────

    public List<Map<String, Object>> getPerformanceTrend(Long studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .collect(Collectors.groupingBy(
                        g -> g.getAcademicYear() + " - " + g.getSemester(),
                        Collectors.averagingDouble(Grade::getGradePoint)))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("period", e.getKey());
                    m.put("gpa",    Math.round(e.getValue() * 100.0) / 100.0);
                    return m;
                })
                .collect(Collectors.toList());
    }

    // ── Student's grades ───────────────────────────────────────────────────────

    public List<GradeDTO.Response> getStudentGrades(Long studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────

    private GradeDTO.Response toResponse(Grade g) {
        return GradeDTO.Response.builder()
                .id(g.getId())
                .studentName(g.getStudent().getFullName())
                .courseCode(g.getCourse().getCourseCode())
                .courseTitle(g.getCourse().getTitle())
                .academicYear(g.getAcademicYear())
                .semester(g.getSemester())
                .attendance(g.getAttendance())
                .assignments(g.getAssignments())
                .projects(g.getProjects())
                .tests(g.getTests())
                .exams(g.getExams())
                .totalScore(g.getTotalScore())
                .letterGrade(g.getLetterGrade())
                .gradePoint(g.getGradePoint())
                .build();
    }
}
