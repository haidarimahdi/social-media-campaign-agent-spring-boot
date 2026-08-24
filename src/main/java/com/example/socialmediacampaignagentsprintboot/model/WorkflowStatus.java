package com.example.socialmediacampaignagentsprintboot.model;

/**
 * Represents the possible statuses in the workflow lifecycle of an entity,
 * such as posts or campaigns, during a social media campaign.
 *
 * Enum Constants:
 * - PENDING: The workflow task is awaiting action or processing.
 * - PLANNED: The workflow task has been scheduled or planned for execution.
 * - DRAFTED: Content or configuration is in the draft stage and is not yet finalized.
 * - REJECTED: The workflow task or content has been reviewed and flagged as unsuitable.
 * - FAILED: Indicates an error or failure occurred during the workflow process.
 * - SAVED_AND_APPROVED: The content or task has been finalized, saved, and approved.
 */
public enum WorkflowStatus {
    PENDING,
    PLANNED,
    DRAFTED,
    REJECTED,
    FAILED,
    SAVED_AND_APPROVED
}
