package com.pecar.academic.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GradeTest {

    @Test
    void computeGrade_A() {
        Grade g = Grade.builder().attendance(95.0).assignments(90.0).projects(92.0).tests(88.0).exams(95.0).build();
        g.computeGrade();
        assertEquals("A", g.getLetterGrade());
        assertEquals(4.0, g.getGradePoint(), 0.01);
    }

    @Test
    void computeGrade_B_plus() {
        Grade g = Grade.builder().attendance(85.0).assignments(80.0).projects(82.0).tests(78.0).exams(80.0).build();
        g.computeGrade();
        assertEquals("B+", g.getLetterGrade());
        assertEquals(3.5, g.getGradePoint(), 0.01);
    }

    @Test
    void computeGrade_B() {
        Grade g = Grade.builder().attendance(75.0).assignments(72.0).projects(70.0).tests(68.0).exams(72.0).build();
        g.computeGrade();
        assertEquals("B", g.getLetterGrade());
        assertEquals(3.0, g.getGradePoint(), 0.01);
    }

    @Test
    void computeGrade_C_plus() {
        Grade g = Grade.builder().attendance(70.0).assignments(68.0).projects(65.0).tests(65.0).exams(66.0).build();
        g.computeGrade();
        assertEquals("C+", g.getLetterGrade());
        assertEquals(2.5, g.getGradePoint(), 0.01);
    }

    @Test
    void computeGrade_C() {
        Grade g = Grade.builder().attendance(60.0).assignments(58.0).projects(55.0).tests(55.0).exams(55.0).build();
        g.computeGrade();
        assertEquals("C", g.getLetterGrade());
        assertEquals(2.0, g.getGradePoint(), 0.01);
    }

    @Test
    void computeGrade_D() {
        Grade g = Grade.builder().attendance(50.0).assignments(48.0).projects(45.0).tests(42.0).exams(45.0).build();
        g.computeGrade();
        assertEquals("D", g.getLetterGrade());
        assertEquals(1.0, g.getGradePoint(), 0.01);
    }

    @Test
    void computeGrade_F() {
        Grade g = Grade.builder().attendance(30.0).assignments(25.0).projects(20.0).tests(15.0).exams(10.0).build();
        g.computeGrade();
        assertEquals("F", g.getLetterGrade());
        assertEquals(0.0, g.getGradePoint(), 0.01);
    }

    @Test
    void computeGrade_boundary90() {
        Grade g = Grade.builder().attendance(100.0).assignments(100.0).projects(100.0).tests(100.0).exams(85.0).build();
        g.computeGrade();
        assertEquals("A", g.getLetterGrade());
        assertEquals(4.0, g.getGradePoint(), 0.01);
    }

    @Test
    void computeGrade_totalScoreFormula() {
        Grade g = Grade.builder().attendance(100.0).assignments(100.0).projects(100.0).tests(100.0).exams(100.0).build();
        g.computeGrade();
        assertEquals(100.0, g.getTotalScore(), 0.01);
        double expected = 100 * 0.10 + 100 * 0.15 + 100 * 0.15 + 100 * 0.20 + 100 * 0.40;
        assertEquals(expected, g.getTotalScore(), 0.01);
    }
}
