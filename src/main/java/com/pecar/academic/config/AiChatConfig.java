package com.pecar.academic.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiChatConfig {

    private static final String SYSTEM_PROMPT = """
            You are the AI Academic Advisor for PECAR University, a helpful assistant for
            students and lecturers. You explain computer science, engineering, business, and
            mathematics concepts clearly and accurately. When asked about graduation requirements
            or specific student records, rely only on the context provided to you in the prompt —
            never invent course codes, grades, or student data. Keep answers concise, well
            organized, and appropriate for a university setting.
            """;

    @Bean
    @ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "openai", matchIfMissing = true)
    public ChatClient openAiChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "google-genai")
    public ChatClient geminiChatClient(
            @Value("${spring.ai.google.genai.api-key:}") String apiKey,
            @Value("${spring.ai.google.genai.chat.options.model:gemini-2.0-flash}") String model,
            @Value("${spring.ai.google.genai.chat.options.temperature:0.7}") double temperature) {

        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .build();

        var genAiClient = com.google.genai.Client.builder()
                .apiKey(apiKey)
                .build();

        GoogleGenAiChatModel chatModel = GoogleGenAiChatModel.builder()
                .genAiClient(genAiClient)
                .defaultOptions(options)
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.ai.model.chat", havingValue = "huggingface")
    public ChatClient huggingfaceChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }
}
