package com.example.socialmediacampaignagentsprintboot.controller.html;

import com.example.socialmediacampaignagentsprintboot.model.*;

import com.example.socialmediacampaignagentsprintboot.service.CampaignMemoryService;
import com.example.socialmediacampaignagentsprintboot.service.CampaignPlanJsonMapper;
import com.example.socialmediacampaignagentsprintboot.service.CampaignPlanService;
import com.example.socialmediacampaignagentsprintboot.service.CampaignWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

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
    private final CampaignPlanJsonMapper jsonMapper;
    private final CampaignPlanService campaignPlanService;

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
    public String index() {
        return "init";
    }

    @GetMapping("/view-plan")
    public String viewPlan(@RequestParam String campaignId, Model model) {
        CampaignPlan plan = memoryService.getPlan(campaignId);

        if (plan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Campaign not found for ID: " + campaignId + ".");
        }

        populateViewModel(model, plan, campaignId);
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

        populateViewModel(model, plan, campaignId);
        return "drafts";
    }

    @PostMapping("/generate-drafts")
    public String approvePlanAndDraft(@RequestParam String planJson,
                                      @RequestParam String campaignId) throws Exception {
        log.info("AI Orchestrator starting Phase 2 (Drafting)...");
        workflowService.approvePlanAndGenerateDraft(campaignId, planJson);
        return "redirect:/view-drafts?campaignId=" + campaignId;
    }

    @PostMapping("/revise-draft")
    public String reviseDraft(@RequestParam String campaignId,
                              @RequestParam int dayNumber,
                              @RequestParam String revisionPrompt) throws Exception {
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


}