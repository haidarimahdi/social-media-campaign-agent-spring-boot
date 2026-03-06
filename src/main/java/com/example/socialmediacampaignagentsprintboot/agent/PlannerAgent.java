package com.example.socialmediacampaignagentsprintboot.agent;

import com.example.socialmediacampaignagentsprintboot.dto.CampaignPlanDTO;
import com.example.socialmediacampaignagentsprintboot.dto.llm.PlannerResponseDTO;
import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Represents a Planner AI Agent responsible for creating concise and structured
 * social media campaign plans tailored to a specific strategic goal.
 *
 * Key Features:
 * - Generates a 3-4 day campaign plan that adheres to strict platform and posting rules.
 * - Limits the campaign to one post per day on either LinkedIn or X (formerly Twitter),
 *   but not both on the same day.
 * - Categorizes each post by 'FunnelStage' (e.g., AWARENESS, CONSIDERATION) and
 *   'ContentPillar' (e.g., EDUCATION, PROMOTION).
 * - Ensures brevity and alignment with business objectives in planning.
 *
 * Methods:
 * - `generatePlan`: Creates a campaign plan for a given objective based on predefined
 *   constraints, generating posts and associating them with their respective
 *   funnel stages, content pillars, and platforms.
 */
public interface PlannerAgent {

    @UserMessage("Create a structured plan for: {{message}}")
    PlannerResponseDTO generatePlan(String message);
}
