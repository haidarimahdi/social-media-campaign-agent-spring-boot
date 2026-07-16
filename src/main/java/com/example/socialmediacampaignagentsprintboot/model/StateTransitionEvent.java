package com.example.socialmediacampaignagentsprintboot.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

/**
 * An immutable record of a single state transition in the campaign workflow.
 * <p>
 * Each event captures the full snapshot of what changed, why it changed, and
 * which agent caused the transition. Together, a sequence of these events forms
 * the append-only JSON State Contract audit trail required by FR1 / NFR1 of the
 * Glass-Box architecture.
 * <p>
 * Fields:
 * - eventId:        Unique identifier for this event (UUID).
 * - campaignId:     The campaign this event belongs to.
 * - dayNumber:      The post day this event relates to (0 = plan-level events).
 * - agent:          The agent or system component that triggered the transition
 *                   (e.g., "OrchestratorAgent", "ReviewerAgent", "HumanOperator").
 * - fromStatus:     The status before the transition (null for initial PLAN_CREATED events).
 * - toStatus:       The status after the transition.
 * - payload:        The full content snapshot at the moment of transition (the draft
 *                   text, QA feedback, or plan JSON). Never null — use empty string
 *                   when no payload is applicable.
 * - timestamp:      UTC instant when the event was recorded.
 */
@Value
@Builder
@Jacksonized
public class StateTransitionEvent {

    String eventId;
    String campaignId;
    int    dayNumber;
    String agent;
    String fromStatus;
    String toStatus;
    String payload;
    Instant timestamp;
}
