package com.example.socialmediacampaignagentsprintboot.config;

import com.example.socialmediacampaignagentsprintboot.agent.CopywriterAgent;
import com.example.socialmediacampaignagentsprintboot.agent.DirectorAgent;
import com.example.socialmediacampaignagentsprintboot.agent.GateKeeperAgent;
import com.example.socialmediacampaignagentsprintboot.agent.PlannerAgent;
import com.example.socialmediacampaignagentsprintboot.service.CampaignTools;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class AgentsConfig {

    @Bean
    PlannerAgent plannerAgent(ChatLanguageModel model) {
        return AiServices.builder(PlannerAgent.class)
                .chatLanguageModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // Planner gets memory for last 10 interactions
                .build();
    }

    @Bean
    CopywriterAgent copywriterAgent(ChatLanguageModel model, ResourceLoader resourceLoader) {
        return AiServices.builder(CopywriterAgent.class)
                .chatLanguageModel(model)
                .systemMessageProvider(chatMemoryId -> {
                    Resource resource = resourceLoader.getResource("classpath:/brand-voice.txt");
                    try {
                        return new String(resource.getInputStream().readAllBytes());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load brand voice guidelines", e);
                    }
                })
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10)) // Copywriter gets memory for last 10 interactions
                .build();
    }

    @Bean
    DirectorAgent directorAgent(ChatLanguageModel model,
                                CampaignTools campaignTools,
                                ResourceLoader resourceLoader) {
        return AiServices.builder(DirectorAgent.class)
                .chatLanguageModel(model)
                .tools(campaignTools) // Director gets tools
                .systemMessageProvider(chatMemoryId -> {
                    Resource resource = resourceLoader.getResource("classpath:prompts/director-system.txt");
                    try {
                        return new String(resource.getInputStream().readAllBytes());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load procedural memory", e);
                    }
                })
                .build();
    }

    @Bean
    GateKeeperAgent gateKeeperAgent(ChatLanguageModel model) {
        return AiServices.builder(GateKeeperAgent.class)
                .chatLanguageModel(model)
                .build();
    }
}
