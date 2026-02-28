package com.example.socialmediacampaignagentsprintboot.controller;

import com.example.socialmediacampaignagentsprintboot.agent.GateKeeperAgent;
import com.example.socialmediacampaignagentsprintboot.agent.PlannerAgent;
import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;

import com.example.socialmediacampaignagentsprintboot.model.ContentPillar;
import com.example.socialmediacampaignagentsprintboot.model.FunnelStage;
import com.example.socialmediacampaignagentsprintboot.model.Platform;
import com.example.socialmediacampaignagentsprintboot.service.CampaignMemoryService;
import com.example.socialmediacampaignagentsprintboot.service.CampaignWorkflowService;
import com.example.socialmediacampaignagentsprintboot.service.FileDebugService;
import com.example.socialmediacampaignagentsprintboot.service.MockSocialMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class AgentController {

    private final PlannerAgent plannerAgent;
    private final GateKeeperAgent gatekeeperAgent;
    private final CampaignMemoryService memoryService;
    private final FileDebugService fileDebugService;
    private final MockSocialMediaService publisher;
    private final CampaignWorkflowService workflowService;


    private void populateViewModel(Model model, CampaignPlan plan, String planJson, String campaignId) {
        model.addAttribute("plan", plan);
        model.addAttribute("planJson", planJson);
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("platforms", Platform.values());
        model.addAttribute("funnelStages", FunnelStage.values());
        model.addAttribute("contentPillars", ContentPillar.values());
    }

    @GetMapping("/")
    public String index(Model model) {
        return "init";
    }

    @PostMapping("/draft-plan")
    public String draftPlan(@RequestParam String goal, Model model) {
        try {
            boolean isValidCampaignRequest = gatekeeperAgent.isMarketingRelated(goal);

            if (!isValidCampaignRequest) {
                model.addAttribute("errorMessage", "I am a Social Media Campaign Agent. " +
                        "I cannot help with '" + goal + "'. Please provide a social media campaign goal.");
                model.addAttribute("currentStep", "INIT");
                return "init";
            }

            System.out.println("📝 Drafting Plan for: " + goal);

            CampaignPlan plan = plannerAgent.generatePlan(goal);
            fileDebugService.savePlan(plan);

            String campaignId = memoryService.savePlan(plan);
            String jsonString = workflowService.serializePlan(plan);
            System.out.println("Plan saved in memory with ID: " + campaignId);

            populateViewModel(model, plan, jsonString, campaignId);
            return "review";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error drafting plan: " + e.getMessage());
            return "init";
        }
    }

    @PostMapping("/generate-drafts")
    public String generateDrafts(@RequestParam String planJson,
                                 @RequestParam String campaignId, Model model) {
        try {
            System.out.println("🎬 Director generating drafts...");

            CampaignPlan approvedPlan = workflowService.parsePlan(planJson);
            if (approvedPlan == null) {
                model.addAttribute("errorMessage", "Session expired or invalid plan data. Please draft a new plan.");
                return "init";
            }

            workflowService.executeDraftingPhase(campaignId, approvedPlan);

            CampaignPlan draftedPlan = memoryService.getPlan(campaignId);
            String draftedJson = workflowService.serializePlan(draftedPlan);

            populateViewModel(model, draftedPlan, draftedJson, campaignId);
            return "drafts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error generating drafts: " + e.getMessage());
            try {
                CampaignPlan failedPlan = workflowService.parsePlan(planJson);
                populateViewModel(model, failedPlan, planJson, campaignId);
                return "review";
            } catch (Exception parseException) {
                // If parsing fails, return to init with error message
                model.addAttribute("errorMessage", "Error processing plan data: " +
                        parseException.getMessage());
                return "init";
            }
        }
    }

    @PostMapping("/revise-draft")
    public String reviseDraft(@RequestParam String campaignId,
                              @RequestParam int dayNumber,
                              @RequestParam Platform platform,
                              @RequestParam String currentDraft,
                              @RequestParam String revisionPrompt, Model model) {
        try {
            System.out.println("🔄 Requesting AI Co-Pilot revision for Day " + dayNumber + " on " + platform.getDisplayName() +
                    " with instruction: " + revisionPrompt);
            CampaignPlan updatedPlan = workflowService.processCopilotRevision(campaignId, dayNumber,
                                                                            platform, currentDraft, revisionPrompt);
            String planJson = workflowService.serializePlan(updatedPlan);

            populateViewModel(model, updatedPlan, planJson, campaignId);
            return "drafts";
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    "AI Revision failed (Timeout/Error). Please try again. " + e.getMessage());
            return "init";
        }
    }

    @PostMapping("/publish-single")
    @ResponseBody
    public String publishSinglePost(@RequestParam String campaignId,
                                  @RequestParam int dayNumber,
                                  @RequestParam Platform platform,
                                  @RequestParam String content) {
        try {
            // Publish using the Mock API which saves to the text file and logs to console
            String result = publisher.publishToPlatform(platform, content);

            memoryService.markPostAsPublished(campaignId, dayNumber);

            CampaignPlan plan = memoryService.getPlan(campaignId);
            plan.getSchedule().get(dayNumber - 1).setGeneratedContent(content);
            memoryService.updatePlan(campaignId, plan);
            return result;
        } catch (Exception e) {
            return "Failed to publish Day " + dayNumber + ": " + e.getMessage();
        }
    }
}