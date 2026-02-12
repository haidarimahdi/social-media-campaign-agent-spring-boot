package com.example.socialmediacampaignagentsprintboot.model;

import java.util.List;

public record CampaignPlan(
        String campaignName,
        String targetAudience,
        String mainGoal,
        List<DailyPost> schedule // A list of the daily actions above
) {}