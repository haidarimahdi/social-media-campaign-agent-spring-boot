package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.agent.CopywriterAgent;
import com.example.socialmediacampaignagentsprintboot.agent.PlannerAgent;
import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import com.example.socialmediacampaignagentsprintboot.model.ContentPillar;
import com.example.socialmediacampaignagentsprintboot.model.FunnelStage;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampaignTools {

    private final PlannerAgent plannerAgent;
    private final CopywriterAgent copywriterAgent;
    private final MockSocialMediaService publisher;

    @Tool("Generates a strategic, multi-day marketing plan based on a high-level goal.")
    public CampaignPlan createStrategicPlan(String goal) {
        System.out.println("AI [Director] is calling the Planner Tool...");
        return plannerAgent.generatePlan(goal);
    }

    @Tool("Writes a specific social media post. Requires platform, audience, funnel stage, pillar, and topic.")
    public String writeSocialMediaPost(String platform, String audience, FunnelStage funnelStage, ContentPillar pillar,
                                       String topic) {
        System.out.println("AI [Director] is calling the Copywriter Tool for " + platform + "...");
        // Minimal safety pause to ensure stream stability
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return copywriterAgent.writePost(platform, audience, funnelStage, pillar, topic);
    }

    @Tool("Publishes the generated content to the specified platform (LinkedIn or X). Returns a success confirmation.")
    public String publishPostToPlatform(String platform, String content) {
        System.out.println("🛠️ AI [Director] is calling the Publisher Tool...");

        // Minimal safety pause to ensure stream stability
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return publisher.publishToPlatform(platform, content);
    }
}



