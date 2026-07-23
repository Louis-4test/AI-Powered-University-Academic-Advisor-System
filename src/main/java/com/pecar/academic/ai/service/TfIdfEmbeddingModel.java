package com.pecar.academic.ai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TfIdfEmbeddingModel implements EmbeddingModel {

    private static final int DIMENSION = 2048;

    private final ConcurrentHashMap<String, AtomicInteger> documentFrequency = new ConcurrentHashMap<>();
    private final AtomicInteger totalDocuments = new AtomicInteger(0);

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        int index = 0;
        for (String text : request.getInstructions()) {
            float[] vector = computeEmbedding(text);
            embeddings.add(new Embedding(vector, index++));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(String text) {
        return computeEmbedding(text);
    }

    @Override
    public float[] embed(Document document) {
        for (String term : tokenize(document.getText())) {
            documentFrequency.computeIfAbsent(term, k -> new AtomicInteger(0)).incrementAndGet();
        }
        totalDocuments.incrementAndGet();
        return computeEmbedding(document.getText());
    }

    private float[] computeEmbedding(String text) {
        Map<String, Integer> tf = new HashMap<>();
        for (String term : tokenize(text)) {
            tf.merge(term, 1, Integer::sum);
        }

        float[] vector = new float[DIMENSION];
        int nDocs = totalDocuments.get();

        for (Map.Entry<String, Integer> entry : tf.entrySet()) {
            int dim = Math.abs(entry.getKey().hashCode()) % DIMENSION;
            int df = documentFrequency.getOrDefault(entry.getKey(), new AtomicInteger(0)).get();
            double idf = df > 0 ? Math.log((double) (nDocs + 1) / (df + 1)) + 1.0 : 1.0;
            vector[dim] += (float) (entry.getValue() * idf);
        }

        float norm = 0f;
        for (float v : vector) norm += v * v;
        norm = (float) Math.sqrt(norm);
        if (norm > 0f) {
            for (int i = 0; i < DIMENSION; i++) {
                vector[i] /= norm;
            }
        }
        return vector;
    }

    private List<String> tokenize(String text) {
        String[] raw = text.toLowerCase().split("[^a-z0-9]+");
        List<String> tokens = new ArrayList<>();
        for (String token : raw) {
            if (token.length() >= 2) {
                tokens.add(token);
            }
        }
        return tokens;
    }
}
