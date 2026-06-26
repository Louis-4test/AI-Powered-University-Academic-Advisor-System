package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.RiskPredictionDTO;
import com.pecar.academic.entity.*;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.GradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskPredictionServiceTest {

    @Mock private GradeRepository gradeRepository;
    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;
    @Mock private ChatClient.PromptUserSpec promptUserSpec;

    @InjectMocks private RiskPredictionService riskPredictionService;

    private Student student;
    private Course course;
    private Grade grade;

    @BeforeEach
    void setUp() {
        student = Student.builder().id(1L).firstName("John").lastName("Doe").build();
        course = Course.builder().id(1L).courseCode("CS101").title("Intro to CS").build();

        grade = Grade.builder()
                .id(1L).student(student).course(course)
                .attendance(90.0).assignments(85.0).projects(80.0)
                .tests(75.0).exams(88.0)
                .build();
        grade.computeGrade();
    }

    @Test
    void predict_excellentPerformance() {
        Grade excellent = Grade.builder()
                .id(2L).student(student).course(course)
                .attendance(98.0).assignments(95.0).projects(96.0)
                .tests(94.0).exams(97.0)
                .build();
        excellent.computeGrade();

        when(gradeRepository.findById(2L)).thenReturn(Optional.of(excellent));
        mockChatClient();

        RiskPredictionDTO.Response result = riskPredictionService.predict(2L);
        assertNotNull(result);
        assertEquals(RiskPredictionDTO.RiskCategory.EXCELLENT_PERFORMANCE, result.getCategory());
        assertTrue(result.getRiskScore() < 20);
    }

    @Test
    void predict_likelyToFail() {
        Grade failing = Grade.builder()
                .id(3L).student(student).course(course)
                .attendance(30.0).assignments(25.0).projects(20.0)
                .tests(15.0).exams(10.0)
                .build();
        failing.computeGrade();

        when(gradeRepository.findById(3L)).thenReturn(Optional.of(failing));
        mockChatClient();

        RiskPredictionDTO.Response result = riskPredictionService.predict(3L);
        assertEquals(RiskPredictionDTO.RiskCategory.LIKELY_TO_FAIL, result.getCategory());
        assertTrue(result.getRiskScore() >= 60);
    }

    @Test
    void predict_gradeNotFound() {
        when(gradeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> riskPredictionService.predict(99L));
    }

    @Test
    void computeRiskScore_highAttendanceLowExams() {
        Grade g = Grade.builder()
                .id(4L).student(student).course(course)
                .attendance(95.0).assignments(80.0).projects(75.0)
                .tests(50.0).exams(40.0)
                .build();
        g.computeGrade();

        when(gradeRepository.findById(4L)).thenReturn(Optional.of(g));
        mockChatClient();

        RiskPredictionDTO.Response result = riskPredictionService.predict(4L);
        // Risk score = 36, category = LIKELY_TO_PASS (riskScore < 40 threshold for AT_RISK)
        assertTrue(result.getRiskScore() > 30);
        assertEquals(RiskPredictionDTO.RiskCategory.LIKELY_TO_PASS, result.getCategory());
    }

    private void mockChatClient() {
        lenient().when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        lenient().when(chatClientRequestSpec.user(any(Consumer.class))).thenAnswer(invocation -> {
            ((Consumer<ChatClient.PromptUserSpec>) invocation.getArgument(0)).accept(promptUserSpec);
            return chatClientRequestSpec;
        });
        lenient().when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        lenient().when(promptUserSpec.text(anyString())).thenReturn(promptUserSpec);
        lenient().when(promptUserSpec.param(anyString(), any())).thenReturn(promptUserSpec);

        RiskPredictionDTO.AiAssessment assessment = new RiskPredictionDTO.AiAssessment(
                RiskPredictionDTO.RiskCategory.LIKELY_TO_PASS, 30, "Good performance", "Keep it up");
        lenient().when(callResponseSpec.entity(RiskPredictionDTO.AiAssessment.class)).thenReturn(assessment);
    }
}
