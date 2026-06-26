package com.pecar.academic.ai.service;

import com.pecar.academic.ai.dto.ExamGeneratorDTO;
import com.pecar.academic.ai.dto.ExamGeneratorDTO.AiExam;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AI Module 4: Automatic Exam Generator.
 *
 * Lecturer specifies a topic, difficulty, and question count; the AI generates
 * a mix of multiple-choice, theory, practical, and case-study questions.
 */
@Service
@RequiredArgsConstructor
public class ExamGeneratorService {

    private final ChatClient chatClient;

    public ExamGeneratorDTO.Response generate(ExamGeneratorDTO.Request request) {
        AiExam exam = chatClient.prompt()
                .user(u -> u.text("""
                        Generate a university-level exam on the topic "{topic}" at {difficulty} difficulty,
                        totaling approximately {count} questions distributed across the following categories:

                        - mcqs: multiple-choice questions, each with exactly 4 options, the correct answer
                          clearly identified, and a short explanation of why it's correct.
                        - theoryQuestions: open-ended conceptual/essay questions, each with brief grading
                          guidance describing what a strong answer should cover.
                        - practicalQuestions: hands-on or applied questions (e.g. write code, design a system,
                          solve a problem), each with a short note on the expected approach.
                        - caseStudies: a realistic scenario followed by a question that asks the student to
                          analyze or respond to that scenario.

                        Distribute the {count} questions reasonably across these four categories based on what
                        best fits the topic — not every topic needs all four types in equal measure, but include
                        at least one of each type where it makes pedagogical sense for "{topic}".
                        """)
                        .param("topic", request.getTopic())
                        .param("difficulty", request.getDifficulty().name())
                        .param("count", request.getQuestionCount()))
                .call()
                .entity(AiExam.class);

        return new ExamGeneratorDTO.Response(
                request.getTopic(),
                request.getDifficulty(),
                request.getQuestionCount(),
                exam.mcqs(),
                exam.theoryQuestions(),
                exam.practicalQuestions(),
                exam.caseStudies()
        );
    }
}
