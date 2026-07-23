package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.ResearchAssistantDTO;
import com.pecar.academic.ai.dto.ResearchAssistantDTO.AiAnalysis;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * AI Module 3: Research Assistant.
 *
 * Workflow: PDF upload → text extraction (Apache PDFBox) → LLM analysis →
 * structured summary / key findings / research gaps / future work.
 */
@Service
@RequiredArgsConstructor
public class ResearchAssistantService {

    /** Roughly caps the extracted text sent to the model to control token cost;
     *  long papers are truncated to their opening + concluding sections. */
    private static final int MAX_CHARS_TO_MODEL = 18_000;

    private final ChatClient chatClient;

    public ResearchAssistantDTO.Response analyze(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())
                && !hasPdfExtension(file.getOriginalFilename())) {
            throw new IllegalArgumentException("Only PDF files are supported for the Research Assistant");
        }

        String extractedText = extractText(file);

        if (extractedText.isBlank()) {
            throw new IllegalArgumentException(
                    "No extractable text was found in this PDF (it may be a scanned image without OCR)");
        }

        String textForModel = truncate(extractedText, MAX_CHARS_TO_MODEL);

        AiAnalysis analysis = chatClient.prompt()
                .user(u -> u.text("""
                        You are analyzing an academic research document. Here is the extracted text:

                        ---
                        {document}
                        ---

                        Provide:
                        1. summary: a concise 3-5 sentence overview of what this document is about.
                        2. keyFindings: the main results or conclusions, as a short paragraph or bullet-style sentences.
                        3. researchGaps: limitations or unanswered questions the document leaves open.
                        4. futureWork: concrete suggestions for follow-up research building on this document.

                        Base everything strictly on the provided text. If the text is incomplete or unclear,
                        say so rather than inventing content.
                        """)
                        .param("document", textForModel))
                .call()
                .entity(AiAnalysis.class);

        return ResearchAssistantDTO.Response.builder()
                .fileName(file.getOriginalFilename())
                .extractedCharacterCount(extractedText.length())
                .summary(analysis.summary())
                .keyFindings(analysis.keyFindings())
                .researchGaps(analysis.researchGaps())
                .futureWork(analysis.futureWork())
                .build();
    }

    // ── PDF text extraction ─────────────────────────────────────────────────────

    private String extractText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("This PDF is encrypted/password-protected and cannot be read");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read PDF file: " + e.getMessage(), e);
        }
    }

    private boolean hasPdfExtension(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    private String truncate(String text, int maxChars) {
        if (text.length() <= maxChars) return text;
        // Keep the introduction and the conclusion — the most information-dense
        // sections for summary/findings/gaps — rather than just chopping the tail.
        int headSize = (int) (maxChars * 0.65);
        int tailSize = maxChars - headSize;
        String head = text.substring(0, headSize);
        String tail = text.substring(text.length() - tailSize);
        return head + "\n\n[... middle section truncated for length ...]\n\n" + tail;
    }
}
