package com.example.socialmediacampaignagentsprintboot.controller;

import com.example.socialmediacampaignagentsprintboot.agent.DirectorAgent;
import com.example.socialmediacampaignagentsprintboot.agent.PlannerAgent;
import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;

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
    private final ObjectMapper objectMapper;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("currentStep", "INIT");
        return "dashboard";
    }

    @PostMapping("/draft-plan")
    public String draftPlan(@RequestParam String goal, Model model) {
        try {
            System.out.println("📝 Phase 1: Drafting Plan for: " + goal);

            CampaignPlan plan = plannerAgent.generatePlan(goal);

            // Convert java Object to JSON string
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(plan);

            model.addAttribute("planJson", jsonString);
            model.addAttribute("currentStep", "REVIEW");

            return "dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error drafting plan: " + e.getMessage());
            model.addAttribute("currentStep", "INIT");
            return "dashboard";
        }
    }

    @PostMapping("/execute-plan")
    public String executePlan(@RequestParam String planJson, Model model) {
        try {
            System.out.println("🎬 Phase 2: Director executing approved plan...");

            // Convert JSON string back to Java Object
            CampaignPlan approvedPlan = objectMapper.readValue(planJson, CampaignPlan.class);

            String report = directorAgent.executeApprovedPlan(approvedPlan);

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
            model.addAttribute("planJson", planJson); // Keep user input
            model.addAttribute("currentStep", "REVIEW");

            return "dashboard";
        }
    }
}