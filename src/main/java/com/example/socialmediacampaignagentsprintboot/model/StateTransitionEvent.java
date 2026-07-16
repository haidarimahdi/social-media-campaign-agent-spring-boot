package com.example.socialmediacampaignagentsprintboot.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a state transition event in the campaign workflow. This class captures the details of a transition
 * from one state to another, including the event type, previous and new statuses, the responsible agent,
 * and any additional payload information.
 * <p>
 *     Fields:
 *     - eventId: A unique identifier for the event.
 *     - timestamp: The timestamp when the event occurred.
 *     - campaignId: The identifier of the campaign associated with the event.
 *     - dayNumber: The day number of the event within the campaign schedule.
 *     - eventType: The type of state transition, such as "PLAN_CREATED" or "POST_DRAFTED".
 *     - fromStatus: The previous status of the workflow before the transition.
 *     - toStatus: The new status of the workflow after the transition.
 *     - agent: The agent responsible for the transition, such as "PlannerAgent" or "CopywriterAgent".
 *     - payload: Additional details about the event, such as generated content or feedback.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StateTransitionEvent {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    @Builder.Default
    private String timestamp = Instant.now().toString();

    private String campaignId;

    private Integer dayNumber;

    private EventType eventType;

    private WorkflowStatus fromStatus;

    private WorkflowStatus toStatus;

    private String agent;

    @Builder.Default
    private Map<String, Object> payload = new HashMap<>();
}
