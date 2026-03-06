package com.example.socialmediacampaignagentsprintboot.model;

import lombok.Getter;

/**
 * Represents the stages of a marketing funnel used in social media campaigns.
 * <p>
 * This enum defines the primary stages of the customer journey and associates
 * each stage with a user-friendly display name. The stages can help in identifying
 * the objectives and content strategy for a specific part of the campaign.
 * <p>
 * Enum Constants:
 * - AWARENESS: Focused on increasing brand visibility and attracting new prospects.
 * - CONSIDERATION: Targeted toward engaging potential customers who are evaluating the brand.
 * - CONVERSION: Designed to drive actions such as purchases or sign-ups.
 * - LOYALTY: Aimed at fostering long-term relationships and retaining existing customers.
 */
@Getter
public enum FunnelStage {
    AWARENESS("Awareness"),
    CONSIDERATION("Consideration"),
    CONVERSION("Conversion"),
    LOYALTY("Loyalty");

    private final String displayName;

    FunnelStage(String displayName) {
        this.displayName = displayName;
    }

}
