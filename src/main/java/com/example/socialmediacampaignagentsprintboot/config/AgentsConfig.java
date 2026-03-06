package com.example.socialmediacampaignagentsprintboot.config;

import com.example.socialmediacampaignagentsprintboot.agent.*;
import com.example.socialmediacampaignagentsprintboot.service.OrchestratorTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Configuration class for setting up AI agents as Spring beans.
 * Each agent is built using the AiServices framework and associated
 * with a ChatLanguageModel to define their interaction logic.
 * Additional dependencies and configurations are provided to cater
 * to each agent's specific needs.
 */
@Configuration
public class AgentsConfig {

    private String loadPrompt(ResourceLoader resourceLoader, String filePath) {
        try {
            Resource resource = resourceLoader.getResource(filePath);
            return new String(resource.getInputStream().readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt from " + filePath, e);
        }
    }

    @Bean
    PlannerAgent plannerAgent(ChatLanguageModel model, ResourceLoader resourceLoader) {
        return AiServices.builder(PlannerAgent.class)
                .chatLanguageModel(model)
                .systemMessageProvider(chatMemory -> {
                    String systemMessage = loadPrompt(resourceLoader, "classpath:/prompts/planner-system.txt");
                    String brandVoice = loadPrompt(resourceLoader, "classpath:/brand-voice.txt");
                    return systemMessage + "\n\n" + brandVoice;
                })
                .build();
    }
    /**
     * Manually creates the ObjectMapper since auto-configuration is failing.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    CopywriterAgent copywriterAgent(ChatLanguageModel model, ResourceLoader resourceLoader) {
        return AiServices.builder(CopywriterAgent.class)
                .chatLanguageModel(model)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .systemMessageProvider(chatMemoryId -> {
                    String systemMessage = loadPrompt(resourceLoader, "classpath:/prompts/copywriter-system.txt");
                    String brandVoice = loadPrompt(resourceLoader, "classpath:/brand-voice.txt");
                    return systemMessage + "\n\n" + brandVoice;
                })
                .build();
    }

    @Bean
    ReviewerAgent reviewerAgent(ChatLanguageModel model, ResourceLoader resourceLoader) {
        return AiServices.builder(ReviewerAgent.class)
                .chatLanguageModel(model)
                .systemMessageProvider(chatMemoryId -> {
                    String systemMessage = loadPrompt(resourceLoader, "classpath:/prompts/reviewer-system.txt");
                    String brandVoice = loadPrompt(resourceLoader, "classpath:/brand-voice.txt");
                    return systemMessage + "\n\n" + brandVoice;
                })
                .build();
    }

    @Bean
    OrchestratorAgent orchestratorAgent(ChatLanguageModel model, OrchestratorTools tools) {
        return AiServices.builder(OrchestratorAgent.class)
                .chatLanguageModel(model)
                // ChatMemory is REQUIRED for ReAct agents to remember tool execution results
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(100))
                .tools(tools)
                .build();
    }

    @Bean
    GateKeeperAgent gateKeeperAgent(ChatLanguageModel model, ResourceLoader resourceLoader) {
        return AiServices.builder(GateKeeperAgent.class)
                .chatLanguageModel(model)
                .systemMessageProvider(chatMemoryId -> {
                    String systemMessage = loadPrompt(resourceLoader, "classpath:/prompts/gatekeeper-system.txt");
                    String brandVoice = loadPrompt(resourceLoader, "classpath:/brand-voice.txt");
                    return systemMessage + "\n\n" + brandVoice;
                })
                .build();
    }
}
