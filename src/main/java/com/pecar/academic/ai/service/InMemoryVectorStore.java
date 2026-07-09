package com.pecar.academic.ai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryVectorStore {

    private final EmbeddingModel embeddingModel;
    private final List<DocumentEntry> entries = new CopyOnWriteArrayList<>();

    public InMemoryVectorStore(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public void add(List<Document> documents) {
        for (Document doc : documents) {
            float[] embedding = embeddingModel.embed(doc);
            entries.add(new DocumentEntry(doc, embedding));
        }
    }

    public List<Document> similaritySearch(String query, int topK) {
        float[] queryEmbedding = embeddingModel.embed(query);

        return entries.stream()
                .map(e -> new ScoredEntry(e, cosineSimilarity(queryEmbedding, e.embedding)))
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .map(e -> {
                    Document doc = e.entry.document;
                    doc.getMetadata().put("distance", e.score);
                    return doc;
                })
                .toList();
    }

    public List<Document> similaritySearch(String query) {
        return similaritySearch(query, 5);
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0 : dot / denom;
    }

    private record DocumentEntry(Document document, float[] embedding) {}

    private record ScoredEntry(DocumentEntry entry, double score) {}
}
