package com.example.socialmediacampaignagentsprintboot.dto.llm;

import com.example.socialmediacampaignagentsprintboot.model.ContentPillar;
import com.example.socialmediacampaignagentsprintboot.model.FunnelStage;
import com.example.socialmediacampaignagentsprintboot.model.Platform;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object representing the response of a Planner module in a social media campaign system.
 * This object contains details about the campaign, its objectives, the target audience,
 * and a schedule of planned posts grouped by day.
 */
@Data
public class PlannerResponseDTO {
    private String campaignName;
    private String targetAudience;
    private String mainGoal;
    private List<PlannedPostDTO> schedule;

    @Data
    public static class PlannedPostDTO {
        private int dayNumber;
        private Platform platform;
        private FunnelStage funnelStage;
        private ContentPillar contentPillar;
        private String topicSummary;
    }

}
