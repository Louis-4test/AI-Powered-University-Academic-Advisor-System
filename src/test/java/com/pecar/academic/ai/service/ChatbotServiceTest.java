package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.ChatbotDTO;
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
class ChatbotServiceTest {

    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;
    @Mock private ChatClient.PromptUserSpec promptUserSpec;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private GradeRepository gradeRepository;

    @InjectMocks private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        lenient().when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        lenient().when(chatClientRequestSpec.user(any(Consumer.class))).thenAnswer(invocation -> {
            ((Consumer<ChatClient.PromptUserSpec>) invocation.getArgument(0)).accept(promptUserSpec);
            return chatClientRequestSpec;
        });
        lenient().when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        lenient().when(callResponseSpec.content()).thenReturn("This is a test response from the AI chatbot.");
        lenient().when(promptUserSpec.text(anyString())).thenReturn(promptUserSpec);
        lenient().when(promptUserSpec.param(anyString(), any())).thenReturn(promptUserSpec);
    }

    @Test
    void ask_withoutStudentId() {
        ChatbotDTO.Request req = new ChatbotDTO.Request();
        req.setQuestion("What is polymorphism?");

        ChatbotDTO.Response response = chatbotService.ask(req);
        assertNotNull(response);
        assertEquals("This is a test response from the AI chatbot.", response.getAnswer());
    }

    @Test
    void ask_withStudentId() {
        Student student = Student.builder().id(1L).firstName("John").lastName("Doe")
                .studentId("STU-001").programName("CS").currentLevel(300)
                .department(Department.builder().id(1L).name("CS").build())
                .build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of());
        when(gradeRepository.calculateCGPA(1L)).thenReturn(Optional.of(3.5));
        when(courseRepository.findByDepartmentId(1L)).thenReturn(List.of());

        ChatbotDTO.Request req = new ChatbotDTO.Request();
        req.setQuestion("Which courses remain?");
        req.setStudentId(1L);

        ChatbotDTO.Response response = chatbotService.ask(req);
        assertNotNull(response);
        verify(studentRepository).findById(1L);
    }

    @Test
    void ask_studentNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        ChatbotDTO.Request req = new ChatbotDTO.Request();
        req.setQuestion("Any question");
        req.setStudentId(99L);

        assertThrows(ResourceNotFoundException.class, () -> chatbotService.ask(req));
    }
}
