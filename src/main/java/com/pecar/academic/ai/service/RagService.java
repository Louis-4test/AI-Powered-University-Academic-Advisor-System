package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.RagDTO;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private static final int CHUNK_SIZE = 800;
    private static final int CHUNK_OVERLAP = 150;
    private static final int MAX_CONTEXT_CHARS = 6000;

    private final InMemoryVectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(InMemoryVectorStore vectorStore, ChatClient chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
    }

    public RagDTO.IngestResponse ingestDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded");
        }

        String text = extractText(file);
        List<String> chunks = splitText(text);
        String filename = file.getOriginalFilename();

        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(chunk, new HashMap<>(Map.of("source", filename != null ? filename : "unknown"))))
                .toList();

        vectorStore.add(documents);

        RagDTO.IngestResponse response = new RagDTO.IngestResponse();
        response.setMessage("Document ingested successfully");
        response.setFileName(filename);
        response.setChunkCount(documents.size());
        return response;
    }

    public RagDTO.QueryResponse query(RagDTO.QueryRequest request) {
        List<Document> relevantDocs = vectorStore.similaritySearch(request.getQuestion());

        String fullContext = relevantDocs.stream()
                .map(d -> d.getText() + "\n(Source: " + d.getMetadata().getOrDefault("source", "unknown") + ")")
                .collect(java.util.stream.Collectors.joining("\n\n---\n\n"));

        String context = fullContext.length() > MAX_CONTEXT_CHARS
                ? fullContext.substring(0, MAX_CONTEXT_CHARS) + "\n...[truncated]"
                : fullContext;

        String answer = chatClient.prompt()
                .user(u -> u.text("""
                        You are a RAG-powered academic advisor assistant. Use the following knowledge base
                        context to answer the question. If the context does not contain enough information
                        to answer the question, say so clearly rather than making up information.

                        Context:
                        {context}

                        Question: {question}
                        """)
                        .param("context", context.isEmpty()
                                ? "No relevant documents found in the knowledge base."
                                : context)
                        .param("question", request.getQuestion()))
                .call()
                .content();

        return RagDTO.QueryResponse.builder()
                .answer(answer)
                .sourceCount(relevantDocs.size())
                .build();
    }

    private String extractText(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase().endsWith(".txt")) {
            try {
                return new String(file.getBytes());
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to read text file", e);
            }
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("PDF is encrypted and cannot be read");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document).trim();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read PDF: " + e.getMessage(), e);
        }
    }

    private List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder current = new StringBuilder();

        for (String para : paragraphs) {
            String trimmed = para.trim();
            if (trimmed.isBlank()) continue;

            if (current.length() + trimmed.length() > CHUNK_SIZE && !current.isEmpty()) {
                chunks.add(current.toString().trim());
                String overlap = current.length() > CHUNK_OVERLAP
                        ? current.substring(current.length() - CHUNK_OVERLAP) + "\n"
                        : "";
                current = new StringBuilder(overlap);
            }
            current.append(trimmed).append("\n\n");
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}
