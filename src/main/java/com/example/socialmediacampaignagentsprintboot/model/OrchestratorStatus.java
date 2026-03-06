package com.example.socialmediacampaignagentsprintboot.model;

/**
 * Represents the various states the orchestrator can be in during the lifecycle
 * of a social media campaign management process.
 * States:
 * - PLAN_READY: Indicates that the campaign plan has been successfully created
 *   and is ready for use.
 * - DRAFTS_READY: Indicates that the drafts for the campaign have been
 *   successfully generated and are ready for review or publishing.
 * - ERROR: Represents a state where the orchestrator encountered an error
 *   during its operations.
 */
public enum OrchestratorStatus {
    //Create a new Enum to represent the exact states the Orchestrator can be in. This eliminates the possibility of the LLM hallucinating a slightly different phrase.
    PLAN_READY,
    DRAFTS_READY,
    ERROR
}
