package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.ContentPillar;
import com.example.socialmediacampaignagentsprintboot.model.FunnelStage;
import com.example.socialmediacampaignagentsprintboot.model.Platform;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DirectorTools {

    private final ContentGraphService graphService;
    private final CampaignMemoryService memoryService;
    private final FileDebugService fileDebugService;

    @Tool("Write a specific social media post. Requires platform, audience, funnel stage, pillar, and topic.")
    public String writeSocialMediaPost(String campaignId, int dayNumber, Platform platform, String audience,
                                       FunnelStage funnelStage, ContentPillar pillar, String topic) {

        if (memoryService.isPostDrafted(campaignId, dayNumber)) {
            return "SKIPPING: Post for Day " + dayNumber + " is already drafted. No need to rewrite.";
        }

        System.out.println("AI [Director] starting workflow for Day " + dayNumber + "...");

        String content = graphService.executeNewPostGraph(platform, audience, funnelStage, pillar, topic);


        var plan = memoryService.getPlan(campaignId);
        plan.getSchedule().get(dayNumber - 1).setGeneratedContent(content);
        memoryService.updatePlan(campaignId, plan);
        memoryService.markPostAsDrafted(campaignId, dayNumber);
        fileDebugService.logPost(platform.getDisplayName(), dayNumber, content);

        return "Successfully drafted post for Day " + dayNumber + ". CRITICAL INSTRUCTION: Proceed to the next day.";
    }


}
