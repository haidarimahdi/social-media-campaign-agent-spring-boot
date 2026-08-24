package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.agent.OrchestratorAgent;
import com.example.socialmediacampaignagentsprintboot.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final AuditTrailService auditTrailService;

    @Async
    public void resumeCampaignGeneration() throws Exception {
        CampaignPlan plan = campaignMemoryService.getActiveCampaignPlanForRecovery();
        executeRecovery(plan);
    }

    @Async
    public void resumeCampaignGeneration(String campaignId) throws Exception {
        CampaignPlan plan = campaignMemoryService.getPlan(campaignId);
        if (plan != null && plan.getCampaignId() == null) {
            plan.setCampaignId(campaignId);
        }
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

        try {
            log.info("[RECOVERY] Attempting to resume campaign generation for Campaign ID: {}", plan.getCampaignId());
            safeTelemetryBroadcast(plan.getCampaignId(), "🔄 CRASH DETECTED: Re-hydrating context and resuming workflow...");

            String currentStateJson = jsonMapper.serializePlan(plan);

            String recoveryPrompt = String.format("""
                    SYSTEM RECOVERY INITIATED.
                    The system crashed during the drafting phase. You are now resuming execution.
                    
                    CRITICAL INSTRUCTION: Your Campaign ID is exactly '%s'. You MUST use this exact ID for all tool calls.
        
                    [CURRENT SYSTEM STATE]:
                    %s
                    
                    Analyze the JSON state above. Identify the days that are marked as 'PENDING' or 'FAILED'.\s
                    Resume your drafting pipeline (using 'draftPost' and 'reviewPost' tools) ONLY for the unfinished days.\s
                    Do not re-draft any day that is already 'DRAFTED' or 'APPROVED'.
                    """, plan.getCampaignId(), currentStateJson);

            orchestratorAgent.draftCampaign(plan.getCampaignId(), recoveryPrompt);
            campaignMemoryService.persistStateToFile();
            log.info("[RECOVERY] Successfully resumed.");
            safeTelemetryBroadcast(plan.getCampaignId(), "✅ Recovery Complete. Resuming normal operations.");
        } catch (Exception e) {
            log.error("[RECOVERY] Orchestrator Agent encountered a fatal error: {}", e.getMessage());
            safeTelemetryBroadcast(plan.getCampaignId(), "❌ FATAL RECOVERY ERROR: " + e.getMessage());
        } finally {
            safeTelemetryBroadcast(plan.getCampaignId(), "GENERATION_COMPLETE");
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

    @Async
    public void approvePlanAndGenerateDraft(String campaignId, String planJson) throws Exception {
       try {
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

           // The drafts are fully approved by the ReviewerAgent, so the final state is ready for the benchmark.
           saveEvaluationLog(campaignId);

       } catch (Exception e) {
           log.error("Drafting pipeline failed for Campaign {}", campaignId, e);
           safeTelemetryBroadcast(campaignId, "❌ ERROR: " + e.getMessage());
       } finally {
           safeTelemetryBroadcast(campaignId, "GENERATION_COMPLETE");
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

    /**
     * Utility method to write the final multi-agent system output to a local file for evaluation.
     */
    private void saveEvaluationLog(String campaignId) {
        try {
            CampaignPlan plan = campaignMemoryService.getPlan(campaignId);
//            String planJson = jsonMapper.serializePlan(plan);

            JsonNode rootNode = jsonMapper.getObjectMapper().valueToTree(plan);

            // Strip out internal state variables from the daily posts
            if (rootNode.has("schedule") && rootNode.get("schedule").isArray()) {
                ArrayNode schedule = (ArrayNode) rootNode.get("schedule");
                for (JsonNode postNode : schedule) {
                    if (postNode.isObject()) {
                        ObjectNode post = (ObjectNode) postNode;
                        // Remove backend-only fields so they don't pollute the evaluation benchmark
                        post.remove("status");
                        post.remove("memoryVersion");
                    }
                }
            }

            String planJson = jsonMapper.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

            Path dirPath = Paths.get("debug_logs", "proposed_system");
            Files.createDirectories(dirPath);

            String filename = "proposed_output_" + campaignId + "_" + Instant.now().toEpochMilli() + ".json";
            Path filePath = dirPath.resolve(filename);

            // Structure the log exactly like the baseline output for easy 1:1 comparison
            String logEntry = "{\n  \"testGoal\": \"" + plan.getMainGoal() + "\",\n  \"proposedOutput\": " + planJson + "\n}";

            Files.writeString(filePath, logEntry, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("[EVALUATION] Proposed system output successfully saved to {}", filePath);
        } catch (Exception e) {
            log.error("[EVALUATION] Failed to save proposed system output: {}", e.getMessage());
        }
    }

    public void rollbackToCheckpoint(String campaignId, String eventId) {
        log.info("[TIME TRAVEL] Initiating localized rollback for Campaign {} to Event ID {}", campaignId, eventId);

        // Fetch the complete execution history
        List<StateTransitionEvent> allEvents = auditTrailService.readEvents(campaignId);

        // Locate the precise historical event
        StateTransitionEvent targetEvent = null;
        for (StateTransitionEvent event : allEvents) {
            if (event.getEventId().equals(eventId)) {
                targetEvent = event;
                break;
            }
        }
        if (targetEvent == null || targetEvent.getDayNumber() == null) {
            throw new IllegalArgumentException("Invalid checkpoint: Event ID not found or lacks a day scope.");
        }

        int targetDay = targetEvent.getDayNumber();

        // Fetch the active plan
        CampaignPlan plan = campaignMemoryService.getPlan(campaignId);
        DailyPost postToRestore = plan.getSchedule().get(targetDay - 1);

        // Surgically apply the historical state ONLY to the targeted day
        postToRestore.setStatus(targetEvent.getToStatus());

        Map<String, Object> payload = targetEvent.getPayload();
        if (payload.containsKey("newContent")) {
            postToRestore.setGeneratedContent((String) payload.get("newContent"));
        } else if (payload.containsKey("generatedContent")) {
            postToRestore.setGeneratedContent((String) payload.get("generatedContent"));
        } else if (payload.containsKey("previousContent")) {
            // Fallback for certain rejection events where newContent isn't set yet
            postToRestore.setGeneratedContent((String) payload.get("previousContent"));
        }

        // Increment the memory version to fork the timeline for this specific day
        postToRestore.setMemoryVersion(postToRestore.getMemoryVersion() + 1);

        // Persist the surgical update
        campaignMemoryService.updatePlan(campaignId, plan);
        campaignMemoryService.persistStateToFile();

        Map<String, Object> rollbackPayload = new HashMap<>();
        rollbackPayload.put("restoredEventId", eventId);
        rollbackPayload.put("targetedDay", targetDay);

        // Log the localized rollback
        StateTransitionEvent rollbackLog = StateTransitionEvent.builder()
                .campaignId(campaignId)
                .dayNumber(targetDay)
                .eventType(EventType.MANUAL_EDIT_SAVED)
                .agent("Human Operator (Rollback)")
                .toStatus(targetEvent.getToStatus())
                .payload(rollbackPayload)
                .build();

        auditTrailService.appendEvent(rollbackLog);

        log.info("[TIME TRAVEL] Surgical rollback complete for Day {}. Memory version incremented to v{}.",
                targetDay, postToRestore.getMemoryVersion());
    }

    /**
     * The "Shout into the Void" method.
     * Attempts to send telemetry to the UI. If the UI is not connected,
     * it catches the error, logs a debug message, and lets the AI continue working safely.
     */
    private void safeTelemetryBroadcast(String campaignId, String message) {
        try {
            if (telemetryService != null) {
                telemetryService.broadcast(campaignId, message);
            }
        } catch (Exception e) {
            // Swallow the exception. The UI is disconnected or racing, but the Glass-Box state machine must survive.
            log.debug("[TELEMETRY DROPPED] UI not listening for Campaign {}. AI continuing in background.", campaignId);
        }
    }

}
