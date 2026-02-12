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
//                .temperature(0.7f)
//                .timeout(Duration.ofSeconds(120))
                .maxRetries(3)
                .build();
    }

    @Bean
    CommandLineRunner debugConnection(ChatLanguageModel model) {
        return args -> {
            System.out.println("🚀 Agent is online.");
        };
    }

//    @Bean
//    @Primary
//    ChatLanguageModel geminiChatModel() {
//        // PROOF OF LIFE: This prints to console if this specific config is loaded
//        System.out.println("--------------------------------------------------");
//        System.out.println("⚡ SETTING TIMEOUT TO 120 SECONDS... ⚡");
//        System.out.println("--------------------------------------------------");
//
//        return GoogleAiGeminiChatModel.builder()
//                .apiKey(geminiApiKey)
////                .modelName("gemini-3-flash-preview")
//                .modelName("gemini-2.5-flash-lite")
////                .modelName("gemini-2.5-flash")
//                .temperature(0.7)
//                .timeout(Duration.ofSeconds(60))
//                .maxRetries(3)
//                .build();
//    }
}