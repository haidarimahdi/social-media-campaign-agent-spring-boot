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

/**
 * Main entry point for the CampaignAgentApplication.
 * This application is designed to integrate and manage campaign agents
 * using Vertex AI capabilities for advanced chat-based language modeling.
 * <p>
 * This class initializes the required configuration and setup for the
 * application, including:
 * - Setting JVM properties for network preference.
 * - Running the Spring Boot application context.
 * - Declaring necessary beans for the application's ecosystem.
 * <p>
 * Key Features:
 * - Configures the integration with Vertex AI for Gemini chat models.
 * - Loads project-specific settings such as project ID and location.
 * - Declares a command-line runner to log the successful application startup.
 * <p>
 * Dependencies:
 * - Spring Boot for application orchestration and*/
@SpringBootApplication
@Slf4j
public class CampaignAgentApplication {

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        SpringApplication.run(CampaignAgentApplication.class, args);
    }

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
        return args -> log.info("🚀 Agent is online.");
    }

}