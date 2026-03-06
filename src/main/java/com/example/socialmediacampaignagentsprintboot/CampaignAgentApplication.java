package com.example.socialmediacampaignagentsprintboot;

import com.example.socialmediacampaignagentsprintboot.config.TokenLoggingListener;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

@SpringBootApplication
@Slf4j
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
    ChatLanguageModel vertexChatModel(TokenLoggingListener tokenListener) {
        System.out.println("--------------------------------------------------");
        System.out.println("⚡ VERTEX AI INITIALIZED (Project: " + projectId + ") ⚡");
        System.out.println("--------------------------------------------------");

        return VertexAiGeminiChatModel.builder()
                .project(projectId)
                .location(location)
                .modelName("gemini-2.5-flash")
                .maxRetries(3)
                .listeners(List.of(tokenListener))
                .build();
    }

    @Bean
    CommandLineRunner debugConnection() {
        return args -> {
            log.info("🚀 Agent is online.");
        };
    }

}