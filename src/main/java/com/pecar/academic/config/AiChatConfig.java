package com.pecar.academic.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are the AI Academic Advisor for PECAR University, a helpful assistant for
                        students and lecturers. You explain computer science, engineering, business, and
                        mathematics concepts clearly and accurately. When asked about graduation requirements
                        or specific student records, rely only on the context provided to you in the prompt —
                        never invent course codes, grades, or student data. Keep answers concise, well
                        organized, and appropriate for a university setting.
                        """)
                .build();
    }
}
