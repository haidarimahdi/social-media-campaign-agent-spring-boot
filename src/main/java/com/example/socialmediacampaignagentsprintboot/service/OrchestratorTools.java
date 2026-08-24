package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.agent.CopywriterAgent;
import com.example.socialmediacampaignagentsprintboot.agent.GateKeeperAgent;
import com.example.socialmediacampaignagentsprintboot.agent.PlannerAgent;
import com.example.socialmediacampaignagentsprintboot.agent.ReviewerAgent;
import com.example.socialmediacampaignagentsprintboot.dto.llm.CopyWriterResponseDTO;
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
import java.util.HashMap;
import java.util.Map;

import static com.example.socialmediacampaignagentsprintboot.model.WorkflowStatus.*;

/**
 * A utility class for orchestrating tasks related to campaign planning, drafting, reviewing,
 * and managing social media content.
 * <p>
 * It interacts with various agents and services to facilitate campaign management,
 * including creating campaign plans, generating and revising social media posts,
 * and tracking campaign progress.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrchestratorTools {

    private final PlannerAgent plannerAgent;
    private final CopywriterAgent copywriterAgent;
    private final ReviewerAgent reviewerAgent;
    private final CampaignMemoryService memoryService;
    private final GateKeeperAgent gateKeeperAgent;
    private final ObjectMapper objectMapper;
    private final AuditTrailService auditTrailService;
    private final SseTelemetryService telemetryService;

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
        contextNode.put("targetAudience", plan.getTargetAudience());
        contextNode.put("platform", post.getPlatform().getDisplayName());
        contextNode.put("funnelStage", post.getFunnelStage().getDisplayName());
        contextNode.put("contentPillar", post.getContentPillar().getDisplayName());
        contextNode.put("topicSummary", post.getTopicSummary());

        return contextNode.toString();
    }

    private void recordTransition(
            String campaignId,
            Integer dayNumber,
            EventType eventType,
            WorkflowStatus fromStatus,
            WorkflowStatus toStatus,
            String agent,
            Map<String, Object> payload
    ) {
        StateTransitionEvent event = StateTransitionEvent.builder()
                .campaignId(campaignId)
                .dayNumber(dayNumber)
                .eventType(eventType)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .agent(agent)
                .payload(payload)
                .build();

        auditTrailService.appendEvent(event);
    }

    @Tool("Checks the current status of all days in the campaign. Returns which days are PENDING, DRAFTED, REJECTED, or APPROVED.")
    public String checkCampaignProgress(String campaignId) {
        CampaignPlan plan = memoryService.getPlan(campaignId);

        if (plan == null) {
            return "ERROR: Campaign not found for ID '" + campaignId + "'." +
                    "Please use the exact Campaign ID provided in your system instructions.";
        }

        StringBuilder progress = new StringBuilder("Campaign Progress:\n");
        for (DailyPost post : plan.getSchedule()) {
            progress.append("Day ").append(post.getDayNumber())
                    .append(" - Status: ").append(post.getStatus() != null ? post.getStatus() : PENDING)
                    .append("\n");
        }
        return progress.toString();
    }

    @Tool("Validates whether the user's goal is marketing-related. MUST be called before creating a plan.")
    public String validateGoal(@P("The user's marketing goal to validate.") String goal) {
        try {
            boolean isMarketing = gateKeeperAgent.isMarketingRelated(goal);
            log.info("[TOOL EXECUTED] GateKeeperAgent validated goal: {}", goal);

            return isMarketing
                    ? "VALID: Goal is marketing-related. Proceed to createCampaignPlan."
                    : "INVALID: Goal is not marketing-related. Terminate the process.";
        } catch (Exception e) {
            log.error("[TOOL EXECUTED] GateKeeperAgent API crashed: {}", e.getMessage());
            return "CRITICAL ERROR: API rate limit or timeout exceeded while validating the goal." +
                    "Stop execution immediately and notify the user.";
        }
    }

    @Tool("Generates a strategic campaign plan (schedule) based on the user's high-level goal. Only call this after " +
            "validateGoal returns VALID.")
    public String createCampaignPlan(
            @P("The exact Campaign ID provided in the system instruction.") String campaignId,
            @P("The marketing goal") String goal) {

        try {

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
            memoryService.persistStateToFile();

            Map<String, Object> payload = new HashMap<>();
            payload.put("campaignName", fullPlan.getCampaignName());
            payload.put("mainGoal", fullPlan.getMainGoal());
            payload.put("targetAudience", fullPlan.getTargetAudience());
            payload.put("scheduleSize", fullPlan.getSchedule() != null ? fullPlan.getSchedule().size() : 0);

            recordTransition(
                    campaignId,
                    null,
                    EventType.PLAN_CREATED,
                    null,
                    WorkflowStatus.PLANNED,
                    "PlannerAgent",
                    payload);


            return "SUCCESS: Campaign Plan created.";
        } catch (Exception e) {
            log.error("[TOOL EXECUTED] PlannerAgent API crashed: {}", e.getMessage());
            return "CRITICAL ERROR: API rate limit or timeout exceeded while generating the plan." +
                    "Stop execution immediately and notify the user.";
        }
    }

    @Tool("Fetches the campaign plan details.")
    public CampaignPlan getCampaignPlan(
            @P("The exact Campaign ID. NEVER invent a new ID.") String campaignId) {
        log.info("[TOOL EXECUTED] Supervisor requested to fetch the Campaign Plan for ID: {}", campaignId);
        return memoryService.getPlan(campaignId);
    }

    @Tool("Drafts a social media post. Use this to create the initial content.")
//    @Retryable(retryFor = JsonMappingException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String draftPost(
            @P("The exact Campaign ID.") String campaignId,
            @P("The day number for this post") int dayNumber) {

        telemetryService.broadcast(campaignId, "📝 CopywriterAgent: Brainstorming and drafting content for Day " + dayNumber + "...");
        CampaignPlan plan = memoryService.getPlan(campaignId);

        if (plan == null) {
            return "CRITICAL ERROR: Campaign Plan not found for ID: " + campaignId + "." +
                    "Please ensure you are using the exact Campaign ID provided in your system instructions.";
        }
        DailyPost post = findPostByDay(plan, dayNumber);

        if (post.getStatus().equals(DRAFTED) || post.getStatus().equals(SAVED_AND_APPROVED)) {
            return "Post for Day " + dayNumber + " has already been drafted or approved. Proceed to the next day.";
        }

        try {
            String blackboardContext = buildBlackboardContext(plan, post);
            String memoryId = campaignId + "_day_" + dayNumber + "_v" + post.getMemoryVersion();

            log.info("[TOOL EXECUTED] Drafting Day {} for Campaign ID {} on Memory Branch {}",
                    dayNumber, campaignId, post.getMemoryVersion());
            CopyWriterResponseDTO generatedDraft = copywriterAgent.writePost(memoryId, blackboardContext);

            WorkflowStatus previousStatus = post.getStatus() != null ? post.getStatus() : WorkflowStatus.PENDING;

            // Update the specific day with the new content AND the new status
            post.setGeneratedContent(generatedDraft.postBody());

            post.setStatus(DRAFTED);

            // Save the updated plan back to memory
            memoryService.updatePlan(campaignId, plan);
            memoryService.persistStateToFile();

            Map<String, Object> payload = new HashMap<>();
            payload.put("platform", post.getPlatform().getDisplayName());
            payload.put("funnelStage", post.getFunnelStage().getDisplayName());
            payload.put("contentPillar", post.getContentPillar().getDisplayName());
            payload.put("topicSummary", post.getTopicSummary());
            payload.put("generatedContent", generatedDraft.postBody());

            recordTransition(
                    campaignId,
                    dayNumber,
                    EventType.POST_DRAFTED,
                    previousStatus,
                    DRAFTED,
                    "CopywriterAgent",
                    payload
            );

            return "SUCCESS: Draft created for Day " + dayNumber + ". State is now DRAFTED. You must now review it.";
        } catch (IllegalArgumentException e) {
            log.warn("[TOOL EXECUTED] Invalid day requested: {}", e.getMessage());

            return "ERROR: " + e.getMessage();
        } catch (Exception e) {
            log.error("Error drafting post for day {}: {}", dayNumber, e.getMessage());
            post.setStatus(FAILED);
            memoryService.updatePlan(campaignId, plan);
            memoryService.persistStateToFile();

            return "CRITICAL ERROR: Failed to draft post for day " + dayNumber + ". Please try again.";
        }
    }

    @Tool("Reviews a drafted post. If approved, it automatically saves it.")
//    @Retryable(retryFor = JsonMappingException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String reviewPost(
            @P("The exact Campaign ID.") String campaignId,
            @P("The day number for this post.") int dayNumber) {

        telemetryService.broadcast(campaignId, "🔍 ReviewerAgent: Auditing Day " + dayNumber + " draft against platform constraints...");

        CampaignPlan plan = memoryService.getPlan(campaignId);

        if (plan == null) {
            return "CRITICAL ERROR: Campaign Plan not found for ID: " + campaignId + "." +
                    "Please ensure you are using the exact Campaign ID provided in your system instructions.";
        }

        DailyPost post = findPostByDay(plan, dayNumber);

        try {
            log.info("[TOOL EXECUTED] QA Review for Day {} for Campaign ID {}", dayNumber, campaignId);
            ReviewResult result = reviewerAgent.reviewPost(post.getPlatform(), post.getGeneratedContent());

            WorkflowStatus previousStatus = post.getStatus();

            if (result.isApproved()) {
                post.setStatus(WorkflowStatus.SAVED_AND_APPROVED);
                memoryService.updatePlan(campaignId, plan);
                memoryService.persistStateToFile();

                Map<String, Object> payload = new HashMap<>();
                payload.put("platform", post.getPlatform().getDisplayName());
                payload.put("generatedContent", post.getGeneratedContent());
                payload.put("reviewOutcome", "APPROVED");

                recordTransition(
                        campaignId,
                        dayNumber,
                        EventType.POST_APPROVED,
                        previousStatus,
                        WorkflowStatus.SAVED_AND_APPROVED,
                        "ReviewerAgent",
                        payload
                );
                return "SUCCESS: Post approved by QA and automatically saved. State is now SAVED_AND_APPROVED.";
            } else {
                post.setStatus(WorkflowStatus.REJECTED);
                memoryService.updatePlan(campaignId, plan);
                memoryService.persistStateToFile();

                Map<String, Object> payload = new HashMap<>();
                payload.put("platform", post.getPlatform().getDisplayName());
                payload.put("generatedContent", post.getGeneratedContent());
                payload.put("reviewOutcome", "REJECTED");
                payload.put("feedback", result.feedback());

                recordTransition(
                        campaignId,
                        dayNumber,
                        EventType.POST_REVIEW_REJECTED,
                        previousStatus,
                        WorkflowStatus.REJECTED,
                        "ReviewerAgent",
                        payload
                );

                return "REJECTED: Post failed QA. State is now REJECTED. Feedback: " + result.feedback() +
                        ". You must now rewrite it using rewriteRejectedPost.";
            }
        } catch (IllegalArgumentException e) {
            log.warn("[TOOL EXECUTED] Invalid day in reviewPost: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        } catch (Exception e) {
            log.error("Error reviewing post for day {}: {}", dayNumber, e.getMessage());
            post.setStatus(FAILED);
            memoryService.updatePlan(campaignId, plan);
            memoryService.persistStateToFile();

            return "CRITICAL ERROR: Failed to draft post for day " + dayNumber + ". Please try again.";
        }
    }

    @Tool("Rewrites a rejected draft using QA feedback.")
//    @Retryable(retryFor = JsonMappingException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String rewriteRejectedPost(
            @P("The exact Campaign ID.") String campaignId,
            @P("The day number for this post.") int dayNumber,
            @P("The feedback from QA") String feedback) {

        telemetryService.broadcast(campaignId, "⚠️ ReviewerAgent: Draft rejected! Feedback: " + feedback);
        telemetryService.broadcast(campaignId, "🔄 CopywriterAgent: Rewriting Day " + dayNumber + " draft to comply with constraints...");

        CampaignPlan plan = memoryService.getPlan(campaignId);

        if (plan == null) {
            return "CRITICAL ERROR: Campaign Plan not found for ID: " + campaignId + "." +
                    "Please ensure you are using the exact Campaign ID provided in your system instructions.";
        }
        DailyPost post = findPostByDay(plan, dayNumber);

        WorkflowStatus previousStatus = post.getStatus();
        String previousContent = post.getGeneratedContent();

        String blackboardContext = buildBlackboardContext(plan, post);
        String memoryId = campaignId + "_day_" + dayNumber + "_v" + post.getMemoryVersion();
        try {

            log.info("[TOOL EXECUTED] Rewriting Day {} for Campaign ID {} on Memory Branch {}",
                    dayNumber, campaignId, post.getMemoryVersion());
            CopyWriterResponseDTO newDraft = copywriterAgent.rewritePost(
                    memoryId, blackboardContext, post.getGeneratedContent(), feedback);

            post.setGeneratedContent(newDraft.postBody());
            post.setStatus(DRAFTED);
            memoryService.updatePlan(campaignId, plan);
            memoryService.persistStateToFile();

            Map<String, Object> payload = new HashMap<>();
            payload.put("platform", post.getPlatform().getDisplayName());
            payload.put("previousContent", previousContent);
            payload.put("newContent", newDraft.postBody());
            payload.put("feedback", feedback);

            recordTransition(
                    campaignId,
                    dayNumber,
                    EventType.POST_REWRITTEN,
                    previousStatus,
                    DRAFTED,
                    "CopywriterAgent",
                    payload
            );

            return "SUCCESS: Rewritten post. State is now DRAFTED. You must now review it again using reviewPost.";
        } catch (IllegalArgumentException e) {
            log.warn("[TOOL EXECUTED] Invalid day in rewriteRejectedPost: {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        } catch (Exception e) {
            log.error("Error rewriting the rejected post for day {}: {}", dayNumber, e.getMessage());
            post.setStatus(FAILED);
            memoryService.updatePlan(campaignId, plan);
            memoryService.persistStateToFile();

            return "CRITICAL ERROR: Failed to draft post for day " + dayNumber + ". Please try again.";
        }
    }

    private DailyPost findPostByDay(CampaignPlan plan, int dayNumber) {
        return plan.getSchedule().stream()
                .filter(p -> p.getDayNumber() == dayNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Day " + dayNumber + " not found in campaign schedule. " +
                                "Valid days: 1–" + plan.getSchedule().size()
                ));
    }
}
