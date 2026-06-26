package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.CareerRecommendationDTO;
import com.pecar.academic.entity.*;
import com.pecar.academic.exception.ResourceNotFoundException;
import com.pecar.academic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareerRecommendationServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;
    @Mock private ChatClient.PromptUserSpec promptUserSpec;

    @InjectMocks private CareerRecommendationService careerRecommendationService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = Student.builder().id(1L).firstName("John").lastName("Doe")
                .studentId("STU-001").programName("B.Sc. CS")
                .currentLevel(300)
                .department(Department.builder().id(1L).name("CS").build())
                .build();
    }

    @Test
    void recommend_success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of());
        when(gradeRepository.findByStudentId(1L)).thenReturn(List.of());
        when(gradeRepository.calculateCGPA(1L)).thenReturn(Optional.of(3.5));

        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(Consumer.class))).thenAnswer(invocation -> {
            ((Consumer<ChatClient.PromptUserSpec>) invocation.getArgument(0)).accept(promptUserSpec);
            return chatClientRequestSpec;
        });
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(promptUserSpec.text(anyString())).thenReturn(promptUserSpec);
        when(promptUserSpec.param(anyString(), any())).thenReturn(promptUserSpec);

        List<CareerRecommendationDTO.CareerSuggestion> careers = List.of(
                new CareerRecommendationDTO.CareerSuggestion("Software Engineer", 90,
                        "Strong match", List.of("Build projects", "Learn more"))
        );
        when(callResponseSpec.entity(CareerRecommendationDTO.AiRecommendation.class))
                .thenReturn(new CareerRecommendationDTO.AiRecommendation(careers));

        CareerRecommendationDTO.Request req = new CareerRecommendationDTO.Request();
        req.setStudentId(1L);

        CareerRecommendationDTO.Response result = careerRecommendationService.recommend(req);
        assertNotNull(result);
        assertEquals(1L, result.studentId());
        assertEquals(1, result.careers().size());
        assertEquals("Software Engineer", result.careers().get(0).careerTitle());
    }

    @Test
    void recommend_studentNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        CareerRecommendationDTO.Request req = new CareerRecommendationDTO.Request();
        req.setStudentId(99L);
        assertThrows(ResourceNotFoundException.class, () -> careerRecommendationService.recommend(req));
    }
}
