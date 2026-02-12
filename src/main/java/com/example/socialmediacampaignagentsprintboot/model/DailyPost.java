package com.example.socialmediacampaignagentsprintboot.model;

import dev.langchain4j.model.output.structured.Description;

public record DailyPost(
        @Description("Day number relative to start (1 = Day 1)")
        int dayNumber,

        @Description("The target social media platform (e.g., LinkedIn, X)")
        String platform,

        @Description("The marketing funnel stage this post targets")
        FunnelStage funnelStage,

        @Description("The strategic theme of the content")
        ContentPillar contentPillar,

        @Description("A brief description of what the post will be about (instructions for the copywriter)")
        String topicSummary
) {}