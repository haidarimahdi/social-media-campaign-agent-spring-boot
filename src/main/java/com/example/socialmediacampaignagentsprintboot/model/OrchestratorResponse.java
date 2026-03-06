package com.example.socialmediacampaignagentsprintboot.model;

import dev.langchain4j.model.output.structured.Description;

public record OrchestratorResponse(
        @Description("The current status of the campaign workflow.")
        OrchestratorStatus status,

        @Description("A message for the user. If the status is ERROR, explain why.")
        String message
) {
}
