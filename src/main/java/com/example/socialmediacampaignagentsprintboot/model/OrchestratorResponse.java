package com.example.socialmediacampaignagentsprintboot.model;

import dev.langchain4j.model.output.structured.Description;

/**
 * Represents the response returned by the orchestrator, providing the current status
 * of a campaign workflow and a corresponding message for the user.
 * <p>
 * Properties:
 * - status: Indicates the current state of the orchestrator, reflecting the
 *   progress or result of the workflow execution. Possible states include:
 *   PLAN_READY, DRAFTS_READY, or ERROR.
 * - message: A descriptive message intended for the user. If the status is ERROR,
 *   the message explains the reason for the error.
 */
public record OrchestratorResponse(
        @Description("The current status of the campaign workflow.")
        OrchestratorStatus status,

        @Description("A message for the user. If the status is ERROR, explain why.")
        String message
) {
}
