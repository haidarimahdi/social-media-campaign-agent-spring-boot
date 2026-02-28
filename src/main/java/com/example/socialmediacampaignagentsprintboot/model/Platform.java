package com.example.socialmediacampaignagentsprintboot.model;

import lombok.Getter;

@Getter
public enum Platform {
    X("X"),
    LINKEDIN("LinkedIn");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

}
