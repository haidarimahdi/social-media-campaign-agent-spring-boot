package com.example.socialmediacampaignagentsprintboot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor  // Required for Jackson JSON deserialization
@AllArgsConstructor
public class CampaignPlan {
    String campaignName;
    String targetAudience;
    String mainGoal;
    List<DailyPost> schedule;
}