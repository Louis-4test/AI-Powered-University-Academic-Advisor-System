package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.ResearchAssistantDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResearchAssistantServiceTest {

    @Mock private ChatClient chatClient;
    @Mock private ChatClient.ChatClientRequestSpec chatClientRequestSpec;
    @Mock private ChatClient.CallResponseSpec callResponseSpec;
    @Mock private ChatClient.PromptUserSpec promptUserSpec;

    @InjectMocks private ResearchAssistantService researchAssistantService;

    @Test
    void analyze_invalidFileType() {
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        assertThrows(IllegalArgumentException.class, () -> researchAssistantService.analyze(file));
    }

    @Test
    void analyze_emptyFile() {
        MultipartFile file = new MockMultipartFile("file", "", "application/pdf", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> researchAssistantService.analyze(file));
    }

    @Test
    void analyze_nullFile() {
        assertThrows(IllegalArgumentException.class, () -> researchAssistantService.analyze(null));
    }

    @Test
    void analyze_success() throws IOException {
        when(chatClient.prompt()).thenReturn(chatClientRequestSpec);
        when(chatClientRequestSpec.user(any(Consumer.class))).thenAnswer(invocation -> {
            ((Consumer<ChatClient.PromptUserSpec>) invocation.getArgument(0)).accept(promptUserSpec);
            return chatClientRequestSpec;
        });
        when(chatClientRequestSpec.call()).thenReturn(callResponseSpec);
        when(promptUserSpec.text(anyString())).thenReturn(promptUserSpec);
        when(promptUserSpec.param(anyString(), any())).thenReturn(promptUserSpec);

        ResearchAssistantDTO.AiAnalysis analysis = new ResearchAssistantDTO.AiAnalysis(
                "Summary text", "Key findings", "Research gaps", "Future work");
        when(callResponseSpec.entity(ResearchAssistantDTO.AiAnalysis.class)).thenReturn(analysis);

        byte[] pdfBytes = createMinimalPdfWithText("This is a test document for analysis.");
        MultipartFile file = new MockMultipartFile("file", "paper.pdf", "application/pdf", pdfBytes);

        ResearchAssistantDTO.Response result = researchAssistantService.analyze(file);
        assertNotNull(result);
        assertEquals("paper.pdf", result.getFileName());
        assertEquals("Summary text", result.getSummary());
    }

    private byte[] createMinimalPdfWithText(String text) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(text);
                contentStream.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}
