package com.example.socialmediacampaignagentsprintboot.controller.html;

import com.example.socialmediacampaignagentsprintboot.dto.AuditTrailViewDTO;
import com.example.socialmediacampaignagentsprintboot.model.*;

import com.example.socialmediacampaignagentsprintboot.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The AgentController class handles HTTP requests for managing campaign workflows,
 * campaign drafts, and manual edits. It acts as the controller in an MVC architecture
 * for managing AI-generated marketing campaigns.
 * <p>
 * Responsibilities:
 * - Starts a new marketing campaign based on a campaign goal.
 * - Provides views for reviewing campaign plans and drafts.
 * - Allows generation of content drafts and approval workflows.
 * - Manages manual editing and revisions of drafts.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AgentController {
    private final CampaignWorkflowService workflowService;
    private final CampaignMemoryService memoryService;
    private final CampaignStateJsonMapper jsonMapper;
    private final CampaignPlanService campaignPlanService;
    private final AuditTrailService auditTrailService;
    private final BrandVoiceService brandVoiceService;

    private void populateViewModel(Model model, CampaignPlan plan, String campaignId) {
        try {
            model.addAttribute("plan", campaignPlanService.toDto(plan));
            model.addAttribute("planJson", jsonMapper.serializePlan(plan));
            model.addAttribute("campaignId", campaignId);
            model.addAttribute("platforms", Platform.values());
            model.addAttribute("funnelStages", FunnelStage.values());
            model.addAttribute("contentPillars", ContentPillar.values());
        } catch (Exception e) {
            log.error("Failed to populate view model for campaignId {}. Exception: {}", campaignId, e.getMessage(), e);
            model.addAttribute("errorMessage", "Unable to load campaign details. " +
                    "The system encountered an error processing the campaign data (likely a DTO conversion or JSON serialization failure).");
        }
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("brandVoice", brandVoiceService.getBrandVoice());
        return "init";
    }

    @PostMapping("/update-brand-voice")
    public String updateBrandVoice(@RequestParam String brandVoiceContent, RedirectAttributes redirectAttributes) {
        brandVoiceService.saveBrandVoice(brandVoiceContent);
        redirectAttributes.addFlashAttribute("successMessage", "Brand voice & rules updated successfully.");
        return "redirect:/";
    }

    @PostMapping("/resume")
    public String resumeGeneration() throws Exception {
        log.info("Initiating Crash Recovery Loop...");
        workflowService.resumeCampaignGeneration();
        CampaignPlan plan = memoryService.getActiveCampaignPlanForRecovery();

        if (plan != null && plan.getCampaignId() != null && plan.getSchedule() != null && !plan.getSchedule().isEmpty()) {
            return "redirect:/view-drafts?campaignId=" + plan.getCampaignId();
        }

        return "redirect:/";
    }

    @GetMapping("/view-plan")
    public String viewPlan(@RequestParam String campaignId, Model model) {
        CampaignProgress progress = memoryService.getCurrentCampaign();

        CampaignPlan plan = memoryService.getPlan(campaignId);

        if (plan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found for ID: " + campaignId + ".");
        }
        populateViewModel(model, plan, campaignId);
        model.addAttribute("campaign", progress);
        return "review";
    }

    @PostMapping("/start-campaign") // Map your init.html form to this
    public String startCampaign(@RequestParam String goal) {
            log.info("AI Orchestrator starting Phase 1 for: {}", goal);
            CampaignPlan plan = workflowService.startCampaign(goal);
            return "redirect:/view-plan?campaignId=" + plan.getCampaignId();
    }

    @GetMapping("/view-drafts")
    public String viewDrafts(@RequestParam String campaignId, Model model) {
        CampaignPlan plan = memoryService.getPlan(campaignId);

        if (plan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found for ID: " + campaignId + ".");
        }


        List<StateTransitionEvent> rawEvents = auditTrailService.readEvents(campaignId);
        Map<Integer, List<AuditTrailViewDTO>> eventsByDay = rawEvents.stream()
                .filter(event -> event.getDayNumber() != null) // Ignore general campaign events
                        .map(event -> AuditTrailViewDTO.builder()
                                .eventId(event.getEventId())
                                .timestamp(event.getTimestamp())
                                .dayNumber(event.getDayNumber())
                                .eventType(event.getEventType())
                                .agent(event.getAgent())
                                .feedback((String) event.getPayload().get("feedback"))
                                .previousContent((String) event.getPayload().get("previousContent"))
                                .newContent((String) event.getPayload().get("newContent"))
                                .reviewOutcome((String) event.getPayload().get("reviewOutcome"))
                                .build())
                .collect(Collectors.groupingBy(AuditTrailViewDTO::getDayNumber));

        populateViewModel(model, plan, campaignId);
        model.addAttribute("auditHistory", eventsByDay);
        return "drafts";
    }

    @PostMapping("/generate-drafts")
    @ResponseBody
    public ResponseEntity<Map<String, String>> approvePlanAndDraft(@RequestParam String planJson,
                                              @RequestParam String campaignId) throws Exception {
        log.info("AI Orchestrator starting Phase 2 (Drafting)...");
        workflowService.approvePlanAndGenerateDraft(campaignId, planJson);
        return ResponseEntity.accepted().body(Map.of("status", "generation_started"));
    }

    @PostMapping("/revise-draft")
    public String reviseDraft(@RequestParam String campaignId,
                              @RequestParam int dayNumber,
                              @RequestParam String revisionPrompt) {
        log.info("AI Orchestrator starting Phase 3 (Human Revision) for Day {}", dayNumber);
        workflowService.reviseDraft(campaignId, dayNumber, revisionPrompt);
        return "redirect:/view-drafts?campaignId=" + campaignId;
    }

    @PostMapping("/save-manual-edit")
    public String saveManualEdit(@RequestParam String campaignId,
                                 @RequestParam int dayNumber,
                                 @RequestParam String editedContent) {
        workflowService.saveManualEdit(campaignId, dayNumber, editedContent);
        return "redirect:/view-drafts?campaignId=" + campaignId;
    }

    @PostMapping("/rollback")
    public String rollbackCampaign(@RequestParam String campaignId,
                                   @RequestParam String eventId) {
        log.info("Human operator triggered a state rollback.");
        workflowService.rollbackToCheckpoint(campaignId, eventId);

        return "redirect:/view-drafts?campaignId=" + campaignId;
    }


}