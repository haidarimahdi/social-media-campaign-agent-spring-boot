package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.agent.OrchestratorAgent;
import com.example.socialmediacampaignagentsprintboot.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service responsible for managing the workflow of campaign creation, planning,
 * drafting, revising, and publishing. This class orchestrates communication
 * between multiple services such as memory storage, debug file handling,
 * AI-based orchestration, and social media publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignWorkflowService {

    private final CampaignMemoryService campaignMemoryService;
    private final CampaignStateJsonMapper jsonMapper;
    private final OrchestratorAgent orchestratorAgent;

    private final MockSocialMediaService mockPublisher;

    private final SseTelemetryService telemetryService;

    public void resumeCampaignGeneration() throws Exception {
        CampaignPlan plan = campaignMemoryService.getActiveCampaignPlanForRecovery();
        executeRecovery(plan);
    }

    public void resumeCampaignGeneration(String campaignId) throws Exception {
        CampaignPlan plan = campaignMemoryService.getPlan(campaignId);
        executeRecovery(plan);
    }

    private void executeRecovery(CampaignPlan plan) throws Exception {
        if (plan == null || plan.getCampaignId() == null) {
            log.warn("[RECOVERY] No valid campaign ID found in memory. Aborting recovery.");
            return;
        }

        if (plan.getSchedule() == null || plan.getSchedule().isEmpty()) {
            log.warn("[RECOVERY] Campaign {} has no schedule. Skipping resume.",  plan.getCampaignId());
            return;
        }

        log.info("[RECOVERY] Attempting to resume campaign generation for Campaign ID: {}", plan.getCampaignId());

        String currentStateJson = jsonMapper.serializePlan(plan);

        String recoveryPrompt = String.format("""
                SYSTEM RECOVERY INITIATED.
                The system crashed during the drafting phase. You are now resuming execution.
                
                [CURRENT SYSTEM STATE]:
                %s
                
                Analyze the JSON state above. Identify the days that are marked as 'PENDING' or 'FAILED'.\s
                Resume your drafting pipeline (using 'draftPost' and 'reviewPost' tools) ONLY for the unfinished days.\s
                Do not re-draft any day that is already 'DRAFTED' or 'APPROVED'.
                """, currentStateJson);
        try {
            orchestratorAgent.draftCampaign(plan.getCampaignId(), recoveryPrompt);
            campaignMemoryService.persistStateToFile();
            log.info("[RECOVERY] Successfully resumed.");
        } catch (Exception e) {
            log.error("[RECOVERY] Orchestrator Agent encountered a fatal error: {}", e.getMessage());
        }
    }
    public CampaignPlan startCampaign(String goal) {


        String campaignId = "CMP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        log.info("🤖 AI Orchestrator starting Phase 1 for the Campaign with ID: {}", campaignId);

        String instruction = String.format("""
                EXECUTE PHASE 1.
                CRITICAL INSTRUCTION: Your Campaign ID is exactly '%s'.
                Goal: %s
                """, campaignId, goal);

        OrchestratorResponse aiResponse = orchestratorAgent.planCampaign(campaignId, instruction);

        if (aiResponse == null) {
            throw new RuntimeException("AI failed to generate plan. Empty or invalid response returned by orchestrator.");
        }

        if (aiResponse.status() == OrchestratorStatus.PLAN_READY) {
            CampaignPlan plan = campaignMemoryService.getPlan(campaignId);

            if (plan == null) {
                throw new RuntimeException("AI indicated plan is ready but no plan found in memory for campaignId: " +
                        campaignId);
            }

            plan.setCampaignId(campaignId);
            campaignMemoryService.updatePlan(campaignId, plan);
            campaignMemoryService.persistStateToFile();

            return plan;
        }
        throw new IllegalArgumentException("Your input is determined as an INVALID marketing objective. Please refine your goal and try again.");
    }

    public void approvePlanAndGenerateDraft(String campaignId, String planJson) throws Exception {
        telemetryService.broadcast(campaignId, "⚙️ Orchestrator: Validating constraints and starting drafting pipeline...");
        CampaignPlan existingPlan = campaignMemoryService.getPlan(campaignId);

        // Check if the AI has already started working on this campaign in the past
        boolean hasProgress = false;
        if (existingPlan != null && existingPlan.getSchedule() != null) {
            hasProgress = existingPlan.getSchedule().stream().anyMatch(post ->
                    post.getStatus() == WorkflowStatus.DRAFTED ||
                            post.getStatus() == WorkflowStatus.SAVED_AND_APPROVED ||
                            post.getStatus() == WorkflowStatus.FAILED
            );
        }

        // If progress exists, the user accidentally clicked again or the system crashed. Resume instead of restarting.
        if (hasProgress) {
            log.info("Accidental re-trigger detected for Campaign {}. Routing to crash recovery.", campaignId);
            resumeCampaignGeneration(campaignId);
            return;
        }

        // Otherwise, perform a normal fresh start
        CampaignPlan approvedPlan = jsonMapper.parsePlan(planJson);
        campaignMemoryService.updatePlan(campaignId, approvedPlan);
        campaignMemoryService.persistStateToFile();

        String instruction = String.format(
                "PLAN_APPROVED. CRITICAL INSTRUCTION: Your Campaign ID is exactly '%s'. " +
                        "Please generate the drafts for the approved plan.", campaignId
        );
        OrchestratorResponse aiResponse = orchestratorAgent.draftCampaign(campaignId, instruction);

        if (aiResponse.status() != OrchestratorStatus.DRAFTS_READY) {
        throw new RuntimeException("AI failed to generate drafts. Status: " + aiResponse.status());
        }
    }

    public void reviseDraft(String campaignId, int dayNumber, String revisionPrompt) {
        String instruction = String.format("""
                HUMAN_REVISION_REQUEST.
                The human user has requested a revision for Day %d.
                User Revision Prompt: '%s'
                """, dayNumber, revisionPrompt);

        OrchestratorResponse aiResponse = orchestratorAgent.handleHumanRevision(campaignId, instruction);

        if (aiResponse.status() == OrchestratorStatus.DRAFTS_READY) {
            campaignMemoryService.persistStateToFile();
            return;
        }
        throw new RuntimeException("AI failed to revise draft. Status: " + aiResponse.status());
    }

    public void saveManualEdit(String campaignId, int dayNumber, String editedContent) {
        CampaignPlan plan = campaignMemoryService.getPlan(campaignId);
        DailyPost post = plan.getSchedule().get(dayNumber - 1);

        post.setGeneratedContent(editedContent);
        post.setStatus(WorkflowStatus.SAVED_AND_APPROVED);

        campaignMemoryService.updatePlan(campaignId, plan);
        campaignMemoryService.persistStateToFile();
    }

    public String publishSinglePost(String campaignId, int dayNumber, String content) {
        CampaignPlan plan = campaignMemoryService.getPlan(campaignId);
        plan.getSchedule().get(dayNumber - 1).setGeneratedContent(content);
        campaignMemoryService.updatePlan(campaignId, plan);
        campaignMemoryService.persistStateToFile();
        DailyPost post = plan.getSchedule().get(dayNumber - 1);
        String result = mockPublisher.publishToPlatform(campaignId, post);
        campaignMemoryService.markPostAsPublished(campaignId, dayNumber);

        return result;
    }


}
