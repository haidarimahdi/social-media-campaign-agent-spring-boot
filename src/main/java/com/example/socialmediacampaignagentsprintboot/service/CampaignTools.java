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

    private final CampaignMemoryService memoryService;
    private final PlannerAgent plannerAgent;
    private final CopywriterAgent copywriterAgent;
    private final MockSocialMediaService publisher;
    private final FileDebugService fileDebugService;

    @Tool("Generates a strategic, multi-day marketing plan based on a high-level goal.")
    public CampaignPlan createStrategicPlan(String goal) {
        System.out.println("AI [Director] is calling the Planner Tool...");
        return plannerAgent.generatePlan(goal);
    }

    @Tool("Writes a specific social media post. Requires platform, audience, funnel stage, pillar, and topic.")
    public String writeSocialMediaPost(String campaignId, int dayNumber, String platform, String audience,
                                       FunnelStage funnelStage, ContentPillar pillar, String topic) {
        if (memoryService.isPostPublished(campaignId, dayNumber)) {
            return "SKIPPING: Post for Day " + dayNumber + " is already published. No need to rewrite.";
        }

        System.out.println("AI [Director] is calling the Copywriter Tool for " + platform + "...");

        String content = copywriterAgent.writePost(platform, audience, funnelStage, pillar, topic);

        fileDebugService.logPost(platform, dayNumber, content);

        return content;
    }

    @Tool("Publishes the generated content to the specified platform (LinkedIn or X). Returns a success confirmation.")
    public String publishPostToPlatform(String campaignId, int dayNumber, String platform, String content) {
        if (memoryService.isPostPublished(campaignId, dayNumber)) {
            return "SKIPPING: Post for Day " + dayNumber + " is already published. No need to republish.";
        }
        System.out.println("🛠️ AI [Director] is calling the Publisher Tool...");

        String result = publisher.publishToPlatform(platform, content);

        memoryService.markPostAsPublished(campaignId, dayNumber);

        return result;
    }
}



