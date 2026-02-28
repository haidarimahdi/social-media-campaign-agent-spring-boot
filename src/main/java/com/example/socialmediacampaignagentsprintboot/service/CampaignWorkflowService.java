package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.agent.DirectorAgent;
import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import com.example.socialmediacampaignagentsprintboot.model.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CampaignWorkflowService {

    private final DirectorAgent directorAgent;
    private final CampaignMemoryService memoryService;
    private final CopilotRevisionService copilotRevisionService;
    private final ObjectMapper objectMapper;

    public CampaignPlan parsePlan(String planJson) throws Exception {
        return objectMapper.readValue(planJson, CampaignPlan.class);
    }

    public  String serializePlan(CampaignPlan plan) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(plan);
    }

    public void executeDraftingPhase(String campaignId, CampaignPlan plan) {
        memoryService.updatePlan(campaignId, plan);
        directorAgent.executeApprovedPlan(campaignId, plan);
    }

    public CampaignPlan processCopilotRevision(String campaignId, int dayNumber, Platform platform, String currentDraft,
                                               String instruction) {
        String revisedContent = copilotRevisionService.reviseAndReviewPost(platform, currentDraft, instruction);

        CampaignPlan plan = memoryService.getPlan(campaignId);
        plan.getSchedule().get(dayNumber - 1).setGeneratedContent(revisedContent);
        memoryService.updatePlan(campaignId, plan);

        return plan;
    }
}
