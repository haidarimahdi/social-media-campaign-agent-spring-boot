package com.example.socialmediacampaignagentsprintboot.model;

import dev.langchain4j.model.output.structured.Description;

/**
 * Represents the result of reviewing a social media post, indicating whether
 * the post complies with platform constraints and providing feedback if necessary.
 * <p>
 * Components:
 * - isApproved: A flag indicating whether the post adheres to all platform-specific
 *   constraints, such as character limits or content guidelines. True if approved,
 *   false if not.
 * - feedback: A string containing specific, actionable feedback when the post is
 *   rejected. The feedback explains the necessary changes. This field remains empty
 *   if the post is approved.
 */
public record ReviewResult (
        @Description("True if the post perfectly follows all platform constraints (e.g., character limits), false otherwise.")
        boolean isApproved,

        @Description("If rejected, provide specific, actionable feedback on what to fix. If approved, leave empty.")
        String feedback
) {}
