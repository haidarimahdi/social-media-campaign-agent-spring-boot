package com.example.socialmediacampaignagentsprintboot.model;

import dev.langchain4j.model.output.structured.Description;

public record ReviewResult (
        @Description("True if the post perfectly follows all platform constraints (e.g., character limits), false otherwise.")
        boolean isApproved,

        @Description("If rejected, provide specific, actionable feedback on what to fix. If approved, leave empty.")
        String feedback
) {}
