package com.example.socialmediacampaignagentsprintboot.model;

import lombok.Data;

@Data
public class GraphState {
    private Platform platform;
    private String currentDraft;
    private String reviewerFeedback;
    private int attemptCount = 0;
    private boolean isApproved = false;

    // Context specific to brand-new posts
    private String audience;
    private FunnelStage funnelStage;
    private ContentPillar contentPillar;
    private String topicSummary;

    // Context specific to Human-in-the-Loop revisions
    private String humanInstruction;
}
