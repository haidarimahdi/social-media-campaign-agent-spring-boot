package com.example.socialmediacampaignagentsprintboot.dto;

import dev.langchain4j.model.output.structured.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
