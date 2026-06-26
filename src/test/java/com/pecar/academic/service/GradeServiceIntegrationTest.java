package com.pecar.academic.service;

import com.pecar.academic.dto.GradeDTO;
import com.pecar.academic.entity.*;
import com.pecar.academic.exception.DuplicateResourceException;
import com.pecar.academic.repository.GradeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for GradeService using an in-memory database.
 * Tests the full service layer with real repositories.
 */
@DataJpaTest
@Import(GradeService.class)
class GradeServiceIntegrationTest {

    @Autowired private GradeService gradeService;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private TestEntityManager em;

    @Test
    void recordAndCalculateCGPA() {
        Department dept = em.persist(Department.builder().name("CS").code("CS").build());
        Student student = em.persist(Student.builder()
                .studentId("STU-TEST-001").firstName("Test").lastName("Student")
                .email("test@student.com").currentLevel(300).build());
        Course course = em.persist(Course.builder()
                .courseCode("TEST101").title("Test Course")
                .creditHours(3).level(300).semester("FIRST")
                .department(dept).build());

        GradeDTO.Request req = new GradeDTO.Request();
        req.setStudentId(student.getId());
        req.setCourseId(course.getId());
        req.setAcademicYear("2025/2026");
        req.setSemester("FIRST");
        req.setAttendance(90.0);
        req.setAssignments(85.0);
        req.setProjects(80.0);
        req.setTests(75.0);
        req.setExams(88.0);

        GradeDTO.Response saved = gradeService.recordGrade(req);
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("B+", saved.getLetterGrade());

        Double cgpa = gradeService.calculateCGPA(student.getId());
        assertTrue(cgpa > 0);

        List<GradeDTO.Response> grades = gradeService.getStudentGrades(student.getId());
        assertEquals(1, grades.size());

        List<Map<String, Object>> ranking = gradeService.getClassRanking();
        assertEquals(1, ranking.size());
    }

    @Test
    void duplicateGradeThrowsException() {
        Department dept = em.persist(Department.builder().name("CS").code("CS").build());
        Student student = em.persist(Student.builder()
                .studentId("STU-TEST-002").firstName("Test").lastName("Student")
                .email("test2@student.com").currentLevel(300).build());
        Course course = em.persist(Course.builder()
                .courseCode("TEST102").title("Test Course 2")
                .creditHours(3).level(300).semester("FIRST")
                .department(dept).build());

        GradeDTO.Request req = new GradeDTO.Request();
        req.setStudentId(student.getId());
        req.setCourseId(course.getId());
        req.setAcademicYear("2025/2026");
        req.setSemester("FIRST");
        req.setAttendance(90.0);
        req.setAssignments(85.0);
        req.setProjects(80.0);
        req.setTests(75.0);
        req.setExams(88.0);

        gradeService.recordGrade(req);
        assertThrows(DuplicateResourceException.class, () -> gradeService.recordGrade(req));
    }
}
