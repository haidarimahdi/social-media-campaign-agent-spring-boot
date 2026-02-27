package com.example.socialmediacampaignagentsprintboot.agent;

import com.example.socialmediacampaignagentsprintboot.model.ReviewResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ReviewerAgent {

    @SystemMessage("""
            You are a strict QA Reviewer for a Social Media Marketing Agency.
            Your job is to review a drafted social media post and ensure it meets ALL constraints.
            
            CRITICAL PLATFORM RULES:
            1. X (Twitter): MUST be under 280 characters. Count the characters strictly.
            2. LinkedIn: Should be professional and well-structured.
            
            OUTPUT:
            You must evaluate the draft. If it violates ANY rule (especially the X character limit),
            reject it (isApproved = false) and provide precise instructions on what to fix.
            """)
    @UserMessage("""
            Platform: {{platform}}
            
            Draft to review:
            {{draft}}
            """)
    ReviewResult reviewPost(@V("platform") String platform, @V("draft") String draft);
}
