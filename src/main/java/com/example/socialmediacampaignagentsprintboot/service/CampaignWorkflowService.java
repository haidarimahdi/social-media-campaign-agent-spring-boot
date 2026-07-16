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

    private final CampaignMemoryService memoryService;
//    private final DebugFileService debugFileService;
    private final MockSocialMediaService publisher;
    private final CampaignPlanJsonMapper jsonMapper;
    private final OrchestratorAgent orchestratorAgent;
    private final OrchestratorTools orchestratorTools;

//    public void resumeCampaignGeneration() {
//        CampaignProgress progress = memoryService.getCurrentCampaign();
//
//        if (progress == null || progress.getPosts() == null) {
//            log.warn("No campaign progress found in memory. Skipping resume.");
//            return;
//        }
//
//        log.info("Attempting to resume campaign generation for Campaign ID: {}", progress.getCampaignId());
//
//        for (DailyPost post : progress.getPosts()) {
//            if (!"DRAFTED".equals(post.getStatus()) && !"APPROVED".equals(post.getStatus())) {
//                log.info("[CRASH RECOVERY] Resuming generation at Day {}", post.getDayNumber());
//
//                try {
//                    orchestratorTools.draftPost(progress.getCampaignId(), post.getDayNumber());
//                    post.setStatus("DRAFTED");
//                    memoryService.persistStateToFile();
//
//                    log.info("[CRASH RECOVERY] Successfully resumed and drafted Day {}", post.getDayNumber());
//                } catch (Exception e) {
//                    log.error("[CRASH RECOVERY] Failed to resume generation for Day {}. Exception: {}", post.getDayNumber(), e.getMessage(), e);
//                    break;
//                }
//            }
//        }
//        log.info("Campaign recovery execution finished.");
//    }


    public CampaignPlan startCampaign(String goal) {


        String campaignId = "CMP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        log.info("🤖 AI Orchestrator starting Phase 1 for the Campaign with ID: {}", campaignId);

        String instruction = String.format("""
                EXECUTE PHASE 1.
                CRITICAL INSTRUCTION: Your Campaign ID is exactly '%s'.
                Goal: %s
                """, campaignId, goal);

//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start("Phase 1: Planning");

        OrchestratorResponse aiResponse = orchestratorAgent.planCampaign(campaignId, instruction);

//        stopWatch.stop();

//        log.info("⏱️ [Execution Time] {} completed in {} ms",
//                stopWatch.getLastTaskName(),
//                stopWatch.getTotalTimeMillis());

        if (aiResponse.status() == OrchestratorStatus.PLAN_READY) {
            CampaignPlan plan = memoryService.getPlan(campaignId);

            if (plan == null) {
                throw new RuntimeException("AI indicated plan is ready but no plan found in memory for campaignId: " +
                        campaignId);
            }

            plan.setCampaignId(campaignId);
            memoryService.updatePlan(campaignId, plan);
            memoryService.persistStateToFile();
//            debugFileService.savePlan(plan);
            return plan;
        }
        throw new RuntimeException("AI failed to generate plan. Status: " + aiResponse.status());
    }

    public void approvePlanAndGenerateDraft(String campaignId, String planJson) throws Exception {
        CampaignPlan approvedPlan = jsonMapper.parsePlan(planJson);
        memoryService.updatePlan(campaignId, approvedPlan);
        memoryService.persistStateToFile();
//        debugFileService.savePlan(approvedPlan);

        OrchestratorResponse aiResponse = orchestratorAgent.draftCampaign(campaignId, "PLAN_APPROVED. Please generate the drafts.");

        if (aiResponse.status() != OrchestratorStatus.DRAFTS_READY) {
        throw new RuntimeException("AI failed to generate drafts. Status: " + aiResponse.status());
        }
        memoryService.getPlan(campaignId);
    }

    public void reviseDraft(String campaignId, int dayNumber, String revisionPrompt) {
        String instruction = String.format("""
                HUMAN_REVISION_REQUEST.
                The human user has requested a revision for Day %d.
                User Revision Prompt: '%s'
                """, dayNumber, revisionPrompt);

        OrchestratorResponse aiResponse = orchestratorAgent.handleHumanRevision(campaignId, instruction);

        if (aiResponse.status() == OrchestratorStatus.DRAFTS_READY) {
            memoryService.getPlan(campaignId);
            return;
        }
        throw new RuntimeException("AI failed to revise draft. Status: " + aiResponse.status());
    }

    public void saveManualEdit(String campaignId, int dayNumber, String editedContent) {
        CampaignPlan plan = memoryService.getPlan(campaignId);
        DailyPost post = plan.getSchedule().get(dayNumber - 1);

        post.setGeneratedContent(editedContent);
        post.setStatus(WorkflowStatus.SAVED_AND_APPROVED);

        memoryService.updatePlan(campaignId, plan);
        memoryService.persistStateToFile();
    }

    public String publishSinglePost(String campaignId, int dayNumber, String content) {
        CampaignPlan plan = memoryService.getPlan(campaignId);
        plan.getSchedule().get(dayNumber - 1).setGeneratedContent(content);
        memoryService.updatePlan(campaignId, plan);
        memoryService.persistStateToFile();
        DailyPost post = plan.getSchedule().get(dayNumber - 1);
        String result = publisher.publishToPlatform(campaignId, post);
        memoryService.markPostAsPublished(campaignId, dayNumber);

        return result;
    }


}
