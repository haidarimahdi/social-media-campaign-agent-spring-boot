package com.example.socialmediacampaignagentsprintboot.model;

import lombok.Getter;

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
