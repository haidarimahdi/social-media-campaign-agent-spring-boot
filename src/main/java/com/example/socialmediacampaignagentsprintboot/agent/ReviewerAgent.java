package com.example.socialmediacampaignagentsprintboot.agent;

import com.example.socialmediacampaignagentsprintboot.model.Platform;
import com.example.socialmediacampaignagentsprintboot.model.ReviewResult;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Represents a Reviewer AI Agent responsible for evaluating drafted social media posts
 * to ensure compliance with platform-specific rules and constraints.
 *<p>
 * Key Responsibilities:
 * - Strictly enforce platform rules, such as character limits and professionalism standards.
 * - Evaluate social media post-drafts and determine whether they meet all defined requirements.
 * - Provide precise and actionable feedback for non-compliant drafts.
 *<p>
 * Platform-Specific Constraints:
 * - X (formerly Twitter): Posts must not exceed 280 characters. Character count is strictly enforced.
 * - LinkedIn: Posts should maintain a professional tone and structure.
 *<p>
 * Methods:
 * - `reviewPost`: Evaluates a social media post draft for a specific platform, checking adherence
 *   to the described rules. Returns the evaluation result, including approval status and feedback
 *   for rejected drafts.
 */
public interface ReviewerAgent {

    @UserMessage("""
            Platform: {{platform}}
            
            Draft to review:
            {{draft}}
            """)
    ReviewResult reviewPost(@V("platform") Platform platform, @V("draft") String draft);
}
