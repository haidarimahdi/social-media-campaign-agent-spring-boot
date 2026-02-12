package com.example.socialmediacampaignagentsprintboot.controller;

import com.example.socialmediacampaignagentsprintboot.ai.DirectorAgent;
import com.example.socialmediacampaignagentsprintboot.ai.PlannerAgent;
import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AgentController {

    private final PlannerAgent plannerAgent;
    private final DirectorAgent directorAgent;

    @GetMapping("/plan")
    public ResponseEntity<?> createPlan(@RequestParam(defaultValue = "Launch a webinar") String goal) {
        try {
            CampaignPlan plan = plannerAgent.generatePlan(goal);
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GetMapping("/full-campaign")
    public ResponseEntity<?> generateFullCampaign(@RequestParam(defaultValue = "Launch a webinar") String goal) {
        long startTime = System.currentTimeMillis();
        try {
            System.out.println("Director Agent taking control for goal: " + goal);

            String agentReport = directorAgent.executeCampaign(goal);

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("✅ Director finished in " + duration + "ms");

            // Format for Frontend: Since the output is now a text report, we wrap it
            Map<String, String> result = new HashMap<>();
//            result.put("day", "ALL");
//            result.put("platform", "Multi-Platform");
//            result.put("strategy", "Autonomous Execution");
            result.put("generated_content", agentReport); // The full report goes here

            return ResponseEntity.ok(List.of(result));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // Helper to unwrap the real error message
    private ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace();

        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        String message = root.getMessage();
        if (message != null && message.toLowerCase().contains("timeout")) {
            return ResponseEntity.status(504).body("❌ Timeout Error: The AI took too long to think. " +
                    "Ensure your custom 120s configuration is loading correctly.");
        }
        return ResponseEntity.status(500).body("❌ Error: " + message);
    }
}