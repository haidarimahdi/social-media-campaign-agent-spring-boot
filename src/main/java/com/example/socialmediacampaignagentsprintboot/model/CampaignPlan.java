package com.example.socialmediacampaignagentsprintboot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jdk.jfr.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor  // Required for Jackson JSON deserialization
@AllArgsConstructor
public class CampaignPlan {

    @JsonIgnore
    private String campaignId;

    @Description("A short, catchy, and creative name for the social media campaign.")
    private String campaignName;

    @Description("The specific demographic or group of people this campaign is targeting.")
    private String targetAudience;

    @Description("The primary objective of the campaign (e.g., Brand Awareness, Product Launch).")
    private String mainGoal;

    @Description("The daily schedule containing exactly one post per day.")
    private List<DailyPost> schedule;
}