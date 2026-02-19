package com.example.socialmediacampaignagentsprintboot.controller;

import com.example.socialmediacampaignagentsprintboot.agent.DirectorAgent;
import com.example.socialmediacampaignagentsprintboot.agent.GateKeeperAgent;
import com.example.socialmediacampaignagentsprintboot.agent.PlannerAgent;
import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;

import com.example.socialmediacampaignagentsprintboot.model.ContentPillar;
import com.example.socialmediacampaignagentsprintboot.model.FunnelStage;
import com.example.socialmediacampaignagentsprintboot.service.CampaignMemoryService;
import com.example.socialmediacampaignagentsprintboot.service.FileDebugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
public class AgentController {

    private final PlannerAgent plannerAgent;
    private final DirectorAgent directorAgent;
    private final GateKeeperAgent gatekeeperAgent;
    private final ObjectMapper objectMapper;
    private final CampaignMemoryService memoryService;
    private final FileDebugService fileDebugService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("currentStep", "INIT");
        return "dashboard";
    }

    @PostMapping("/draft-plan")
    public String draftPlan(@RequestParam String goal, Model model) {
        try {
            boolean isValidCampaignRequest = gatekeeperAgent.isMarketingRelated(goal);

            if (!isValidCampaignRequest) {
                model.addAttribute("errorMessage", "I am a Social Media Campaign Agent. " +
                        "I cannot help with '" + goal + "'. Please provide a social media campaign goal.");
                model.addAttribute("currentStep", "INIT");

                return "dashboard";
            }

            System.out.println("📝 Phase 1: Drafting Plan for: " + goal);

            CampaignPlan plan = plannerAgent.generatePlan(goal);

            fileDebugService.savePlan(plan);

            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(plan);
            String campaignId = memoryService.savePlan(plan);
            System.out.println("Plan saved in memory with ID: " + campaignId);

            model.addAttribute("plan", plan);
            model.addAttribute("funnelStages", FunnelStage.values());
            model.addAttribute("contentPillars", ContentPillar.values());
            model.addAttribute("planJson", jsonString);
            model.addAttribute("campaignId", campaignId);
            model.addAttribute("currentStep", "REVIEW");

            return "dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error drafting plan: " + e.getMessage());
            model.addAttribute("currentStep", "INIT");
            return "dashboard";
        }
    }

    @PostMapping("/execute-plan")
    public String executePlan(@RequestParam String planJson,
                              @RequestParam(required = false) String campaignId,
                              Model model) {
        try {
            System.out.println("🎬 Phase 2: Director executing approved plan...");

            // Convert JSON string back to Java Object
            CampaignPlan approvedPlan = objectMapper.readValue(planJson, CampaignPlan.class);

            if (campaignId == null || campaignId.isEmpty()) {
                model.addAttribute("errorMessage", "Session expired or invalid campaign ID. Please draft a new plan.");
                model.addAttribute("currentStep", "INIT");
                return "dashboard";
            }

            memoryService.updatePlan(campaignId, approvedPlan);


            String report = directorAgent.executeApprovedPlan(campaignId, approvedPlan);

            model.addAttribute("executionReport", report);
            model.addAttribute("currentStep", "RESULT");

            return "dashboard";
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String msg = root.getMessage();
            if (msg != null && msg.toLowerCase().contains("timeout")) {
                msg = "Timeout Error: The AI took too long. Check configuration.";
            }

            model.addAttribute("errorMessage", "Error executing plan: " + msg);
            // PRESERVE STATE: Send the edited JSON back so the user doesn't lose their work
            model.addAttribute("planJson", planJson);

            // Re-bind the object so the Thymeleaf table renders the user's edits
            try {
                CampaignPlan failedPlan = objectMapper.readValue(planJson, CampaignPlan.class);
                model.addAttribute("plan", failedPlan);
                model.addAttribute("funnelStages", FunnelStage.values());
                model.addAttribute("contentPillars", ContentPillar.values());
                model.addAttribute("campaignId", campaignId);
            } catch (Exception parseEx) {
                parseEx.printStackTrace();
            }
            model.addAttribute("currentStep", "REVIEW");
            return "dashboard";
        }
    }
}