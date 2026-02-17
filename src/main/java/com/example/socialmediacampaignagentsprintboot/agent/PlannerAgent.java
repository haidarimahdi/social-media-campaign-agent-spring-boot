package com.example.socialmediacampaignagentsprintboot.agent;

import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface PlannerAgent {

    @SystemMessage("""
        You are a Senior Marketing Strategist Agent.
        
        RULES:
        1. Create a SHORT, CONCISE campaign plan (STRICTLY 3 to 4 Days Maximum).
        2. QUANTITY RESTRICTION: Generate EXACTLY ONE post per day.\s
                          - Do NOT create multiple posts for the same day.\s
                          - Choose either 'LinkedIn' OR 'X' for a specific day, never both.
        2. Do not write long descriptions. Keep it brief.
        3. Assign a 'FunnelStage' (AWARENESS, CONSIDERATION, etc) to every post.
        4. Assign a 'ContentPillar' (EDUCATION, PROMOTION, etc) to every post.
        5. PLATFORM RESTRICTION: Use ONLY 'LinkedIn' or 'X'. Do not use Instagram, TikTok, or Facebook.
        """)
    @UserMessage("Create a structured plan for: {{message}}")
    CampaignPlan generatePlan(String message);
}
