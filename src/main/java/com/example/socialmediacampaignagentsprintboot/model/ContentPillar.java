package com.example.socialmediacampaignagentsprintboot.model;

import lombok.Getter;

/**
 * Represents the different strategic themes or categories of content
 * that can be used in a social media campaign.
 * <p>
 * Each enum value corresponds to a specific content category
 * and has an associated display name for user-friendly descriptions.
 * <p>
 * Enum Constants:
 * - EDUCATION: Content aimed at educating the audience on a specific topic or skill.
 * - ENTERTAINMENT: Content intended to entertain and engage the audience.
 * - PROMOTION: Content focused on promoting products, services, or offers.
 * - BEHIND_THE_SCENES: Content offering a behind-the-scenes look at a company, event, or process.
 * - USER_GENERATED_CONTENT: Content created by users or customers, highlighting their engagement with the brand.
 */
@Getter
public enum ContentPillar {
    EDUCATION("Education"),
    ENTERTAINMENT("Entertainment"),
    PROMOTION("Promotion"),
    BEHIND_THE_SCENES("Behind the Scenes"),
    USER_GENERATED_CONTENT("User-Generated Content");

    private final String displayName;

    ContentPillar(String displayName) {
        this.displayName = displayName;
    }
}
