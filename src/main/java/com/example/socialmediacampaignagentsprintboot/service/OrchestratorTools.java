package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.agent.CopywriterAgent;
import com.example.socialmediacampaignagentsprintboot.agent.GateKeeperAgent;
import com.example.socialmediacampaignagentsprintboot.agent.PlannerAgent;
import com.example.socialmediacampaignagentsprintboot.agent.ReviewerAgent;
import com.example.socialmediacampaignagentsprintboot.dto.llm.PlannerResponseDTO;
import com.example.socialmediacampaignagentsprintboot.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrchestratorTools {

    private final PlannerAgent plannerAgent;
    private final CopywriterAgent copywriterAgent;
    private final ReviewerAgent reviewerAgent;
    private final CampaignMemoryService memoryService;
    private final DebugFileService debugFileService;
    private final GateKeeperAgent gateKeeperAgent;
    private final ObjectMapper objectMapper;

    /**
     * Builds a structured context JSON string for the blackboard system based on the campaign plan
     * and daily post details provided.
     *
     * @param plan The campaign plan containing high-level details, including the main goal.
     * @param post The daily post containing details such as target audience, platform, funnel stage,
     *             content pillar, and topic summary.
     * @return A JSON string representing the blackboard context with key campaign and post details.
     */
    private String buildBlackboardContext(CampaignPlan plan, DailyPost post) {
        ObjectNode contextNode = objectMapper.createObjectNode();
        contextNode.put("campaignGoal", plan.getMainGoal());
        contextNode.put("targetAudience", post.getTargetAudience());
        contextNode.put("platform", post.getPlatform().getDisplayName());
        contextNode.put("funnelStage", post.getFunnelStage().getDisplayName());
        contextNode.put("contentPillar", post.getContentPillar().getDisplayName());
        contextNode.put("topicSummary", post.getTopicSummary());

        return contextNode.toString();
    }

    @Tool("Checks the current status of all days in the campaign. Returns which days are PENDING, DRAFTED, REJECTED, or APPROVED.")
    public String checkCampaignProgress(String campaignId) {
        CampaignPlan plan = memoryService.getPlan(campaignId);
        StringBuilder progress = new StringBuilder("Campaign Progress:\n");
        for (DailyPost post : plan.getSchedule()) {
            progress.append("Day ").append(post.getDayNumber())
                    .append(" - Status: ").append(post.getStatus() != null ? post.getStatus() : "PENDING")
                    .append("\n");
        }
        return progress.toString();
    }

    @Tool("Generates a strategic campaign plan (schedule) based on the user's high-level goal.")
    public String createCampaignPlan(
            @P("The exact Campaign ID provided in the system instruction.") String campaignId,
            @P("The marketing goal") String goal) {

        boolean isMarketing = gateKeeperAgent.isMarketingRelated(goal);

        if (!isMarketing) {
            return "ERROR: the goal is not related to marketing or campaign planning. Terminate the process.";
        }

        log.info("[TOOL EXECUTED] Supervisor requested a Campaign Plan for ID: {}", campaignId);
        PlannerResponseDTO leanPlan = plannerAgent.generatePlan(goal);

        CampaignPlan fullPlan = new CampaignPlan();
        fullPlan.setCampaignName(leanPlan.getCampaignName());
        fullPlan.setMainGoal(leanPlan.getMainGoal());
        fullPlan.setTargetAudience(leanPlan.getTargetAudience());
        
        List<DailyPost> dailyPosts = new ArrayList<>();
        for (PlannerResponseDTO.PlannedPostDTO dto : leanPlan.getSchedule()) {
            DailyPost post = new DailyPost();
            post.setDayNumber(dto.getDayNumber());
            post.setPlatform(dto.getPlatform());
            post.setFunnelStage(dto.getFunnelStage());
            post.setContentPillar(dto.getContentPillar());
            post.setTopicSummary(dto.getTopicSummary());
            dailyPosts.add(post);
        }
        fullPlan.setSchedule(dailyPosts);
        
        memoryService.updatePlan(campaignId, fullPlan);

        return "SUCCESS: Campaign Plan created.";
    }

    @Tool("Fetches the campaign plan details.")
    public CampaignPlan getCampaignPlan(
            @P("The exact Campaign ID. NEVER invent a new ID.") String campaignId) {
        System.out.println("🛠️ [TOOL EXECUTED] Supervisor requested to fetch the Campaign Plan for ID: " + campaignId);
        return memoryService.getPlan(campaignId);
    }

    @Tool("Drafts a social media post. Use this to create the initial content.")
    public String draftPost(
            @P("The exact Campaign ID.") String campaignId,
            @P("The day number for this post") int dayNumber) {

        try {
            CampaignPlan plan = memoryService.getPlan(campaignId);
            DailyPost post = plan.getSchedule().get(dayNumber - 1);

            String blackboardContext = buildBlackboardContext(plan, post);
            String memoryId = campaignId + "_day_" + dayNumber;

            log.info("[TOOL EXECUTED] Drafting Day {} for Campaign ID {}", dayNumber, campaignId);
            String generatedDraft = copywriterAgent.writePost(memoryId, blackboardContext);

            // Update the specific day with the new content AND the new status
            post.setGeneratedContent(generatedDraft);
            post.setStatus("DRAFTED");


            // Save the updated plan back to memory
            memoryService.updatePlan(campaignId, plan);

            return "SUCCESS: Draft created for Day " + dayNumber + ". State is now DRAFTED. You must now review it.";
        } catch (Exception e) {
            log.error("Error drafting post for day {}: {}", dayNumber, e.getMessage());
            return "ERROR: Failed to draft post for day " + dayNumber + ". Please try again.";
        }
    }

    @Tool("Reviews a drafted post. If approved, it automatically saves it.")
    public String reviewPost(
            @P("The exact Campaign ID.") String campaignId,
            @P("The day number for this post.") int dayNumber) {

        CampaignPlan plan = memoryService.getPlan(campaignId);
        DailyPost post = plan.getSchedule().get(dayNumber - 1);

        log.info("[TOOL EXECUTED] QA Review for Day {} for Campaign ID {}", dayNumber, campaignId);
        ReviewResult result = reviewerAgent.reviewPost(post.getPlatform(), post.getGeneratedContent());

        if (result.isApproved()) {
            post.setStatus("SAVED_AND_APPROVED");
            memoryService.updatePlan(campaignId, plan);
            debugFileService.logPost(post.getPlatform().getDisplayName(), dayNumber, post.getGeneratedContent());
            return "SUCCESS: Post approved by QA and automatically saved. State is now SAVED_AND_APPROVED.";
        } else {
            post.setStatus("REJECTED");
            memoryService.updatePlan(campaignId, plan);
            return "REJECTED: Post failed QA. State is now REJECTED. Feedback: " + result.feedback() +
                    ". You must now rewrite it using rewriteRejectedPost.";
        }
    }

    @Tool("Rewrites a rejected draft using QA feedback.")
    public String rewriteRejectedPost(
            @P("The exact Campaign ID.") String campaignId,
            @P("The day number for this post.") int dayNumber,
            @P("The feedback from QA") String feedback) {

        CampaignPlan plan = memoryService.getPlan(campaignId);
        DailyPost post = plan.getSchedule().get(dayNumber - 1);

        String blackboardContext = buildBlackboardContext(plan, post);
        String memoryId = campaignId + "_day_" + dayNumber;

        log.info("[TOOL EXECUTED] Rewriting Day {} for Campaign ID {}", dayNumber, campaignId);
        String newDraft = copywriterAgent.rewritePost(memoryId, blackboardContext, post.getGeneratedContent(), feedback);

        post.setGeneratedContent(newDraft);
        post.setStatus("DRAFTED");

        memoryService.updatePlan(campaignId, plan);
        return "SUCCESS: Rewritten post. State is now DRAFTED. You must now review it again using reviewPost.";
    }
}
