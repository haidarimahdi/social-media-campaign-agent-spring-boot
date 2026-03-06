package com.example.socialmediacampaignagentsprintboot.model;

import lombok.Getter;

/**
 * Represents a platform that can be targeted in a social media campaign.
 * <p>
 * Each platform is associated with a user-friendly display name for better identification.
 * It is used in campaign planning and execution to specify the platform for which posts
 * and strategies are intended.
 * <p>
 * Enum Constants:
 * - X: Represents the social media platform X, previously known as Twitter.
 * - LINKEDIN: Represents the LinkedIn platform, primarily designed for professional networking.
 */
@Getter
public enum Platform {
    X("X"),
    LINKEDIN("LinkedIn");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

}
