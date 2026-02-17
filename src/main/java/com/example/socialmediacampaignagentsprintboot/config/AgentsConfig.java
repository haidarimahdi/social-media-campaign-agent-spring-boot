package com.example.socialmediacampaignagentsprintboot.config;

import com.example.socialmediacampaignagentsprintboot.agent.CopywriterAgent;
import com.example.socialmediacampaignagentsprintboot.agent.DirectorAgent;
import com.example.socialmediacampaignagentsprintboot.agent.PlannerAgent;
import com.example.socialmediacampaignagentsprintboot.service.CampaignTools;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentsConfig {

    @Bean
    PlannerAgent plannerAgent(ChatLanguageModel model) {
        return AiServices.builder(PlannerAgent.class)
                .chatLanguageModel(model)
                .build();
    }

    @Bean
    CopywriterAgent copywriterAgent(ChatLanguageModel model) {
        return AiServices.builder(CopywriterAgent.class)
                .chatLanguageModel(model)
                .build();
    }

    @Bean
    DirectorAgent directorAgent(ChatLanguageModel model, CampaignTools campaignTools) {
        return AiServices.builder(DirectorAgent.class)
                .chatLanguageModel(model)
                .tools(campaignTools) // Director gets tools
                .build();
    }
}
