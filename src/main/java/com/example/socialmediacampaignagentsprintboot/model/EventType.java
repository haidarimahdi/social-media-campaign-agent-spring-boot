package com.example.socialmediacampaignagentsprintboot.model;

/**
 * Represents the different types of events that can occur during the lifecycle of a social media campaign.
 * <p>
 * Enum Constants:
 * - PLAN_CREATED: Indicates that a campaign plan has been created.
 * - POST_DRAFTED: Indicates that a new draft for a social media post has been created.
 * - POST_REVIEW_REJECTED: Indicates that a post draft was reviewed and rejected.
 * - POST_REWRITTEN: Indicates that a rejected post draft has been rewritten.
 * - POST_APPROVED: Indicates that a post draft has been reviewed and approved.
 * - MANUAL_EDIT_SAVED: Indicates that manual edits to a post have been saved.
 * - HITL_APPROVED: Indicates approval by a Human-In-The-Loop (HITL) reviewer.
 * - HITL_REJECTED: Indicates rejection by a Human-In-The-Loop (HITL) reviewer.
 */
public enum EventType {
    PLAN_CREATED,
    POST_DRAFTED,
    POST_REVIEW_REJECTED,
    POST_REWRITTEN,
    POST_APPROVED,
    MANUAL_EDIT_SAVED,
    HITL_APPROVED,
    HITL_REJECTED
}
