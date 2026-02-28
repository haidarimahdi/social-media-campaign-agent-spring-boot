package com.example.socialmediacampaignagentsprintboot;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
public class CampaignAgentApplication {

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        SpringApplication.run(CampaignAgentApplication.class, args);
    }

    // Configuration Properties for Gemini AI
//    @Value("${gemini.api-key}")
//    private String geminiApiKey;

    // Configuration Properties for Vertex AI
    @Value("${vertex.project-id}")
    private String projectId;

    @Value("${vertex.location}")
    private String location;


    @Bean
    @Primary
    ChatLanguageModel vertexChatModel() {
        System.out.println("--------------------------------------------------");
        System.out.println("⚡ VERTEX AI INITIALIZED (Project: " + projectId + ") ⚡");
        System.out.println("--------------------------------------------------");

        return VertexAiGeminiChatModel.builder()
                .project(projectId)
                .location(location)
                .modelName("gemini-2.5-flash")
                .maxRetries(3)
                .build();
    }

    @Bean
    CommandLineRunner debugConnection(ChatLanguageModel model) {
        return args -> {
            System.out.println("🚀 Agent is online.");
        };
    }

}