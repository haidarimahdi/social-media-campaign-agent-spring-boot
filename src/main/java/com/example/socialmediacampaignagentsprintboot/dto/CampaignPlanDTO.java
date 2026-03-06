package com.example.socialmediacampaignagentsprintboot.dto;

import dev.langchain4j.model.output.structured.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object representing the plan for a social media campaign.
 * This class encapsulates the core information required to define a campaign,
 * including its name, target audience, main goal, and daily posting schedule.
 * <p>
 * Fields:
 * - campaignName: A short and creative name for the campaign.
 * - targetAudience: Specifies the demographic or group the campaign is aimed at.
 * - mainGoal: Defines the primary objective of the campaign.
 * - schedule: A list of daily posts scheduled as part of the campaign, each represented
 *   by an instance of {@link DailyPostDTO}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignPlanDTO {
    @Description("A short, catchy, and creative name for the social media campaign.")
    String campaignName;

    @Description("The specific demographic or group of people this campaign is targeting.")
    String targetAudience;

    @Description("The primary objective of the campaign.")
    String mainGoal;

    @Description("The daily schedule strategy.")
    List<DailyPostDTO> schedule;
}
