package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.ExamGeneratorDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamGeneratorServiceTest {

    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;
    @Mock private ChatClient.PromptUserSpec promptUserSpec;

    @InjectMocks private ExamGeneratorService examGeneratorService;

    @Test
    void generate_success() {
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(Consumer.class))).thenAnswer(invocation -> {
            ((Consumer<ChatClient.PromptUserSpec>) invocation.getArgument(0)).accept(promptUserSpec);
            return chatClientRequestSpec;
        });
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(promptUserSpec.text(anyString())).thenReturn(promptUserSpec);
        when(promptUserSpec.param(anyString(), any())).thenReturn(promptUserSpec);

        ExamGeneratorDTO.AiExam exam = new ExamGeneratorDTO.AiExam(
                List.of(new ExamGeneratorDTO.McqQuestion("Q1", List.of("A", "B", "C", "D"), "A", "Explanation")),
                List.of(new ExamGeneratorDTO.TheoryQuestion("Theory Q?", "Guidance")),
                List.of(new ExamGeneratorDTO.PracticalQuestion("Practical Q?", "Approach")),
                List.of(new ExamGeneratorDTO.CaseStudy("Scenario", "Question"))
        );
        when(callResponseSpec.entity(ExamGeneratorDTO.AiExam.class)).thenReturn(exam);

        ExamGeneratorDTO.Request req = new ExamGeneratorDTO.Request();
        req.setTopic("Network Security");
        req.setDifficulty(ExamGeneratorDTO.Difficulty.ADVANCED);
        req.setQuestionCount(10);

        ExamGeneratorDTO.Response result = examGeneratorService.generate(req);
        assertNotNull(result);
        assertEquals("Network Security", result.topic());
        assertEquals(ExamGeneratorDTO.Difficulty.ADVANCED, result.difficulty());
        assertEquals(1, result.mcqs().size());
        assertEquals(1, result.theoryQuestions().size());
        assertEquals(1, result.practicalQuestions().size());
        assertEquals(1, result.caseStudies().size());
    }
}
