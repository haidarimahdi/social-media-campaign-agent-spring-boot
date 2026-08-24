package com.example.socialmediacampaignagentsprintboot.dto;

import com.example.socialmediacampaignagentsprintboot.model.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object for representing audit trail events in the system.
 * This class encapsulates essential details about a specific activity or event
 * occurring within the application, primarily for logging and tracking purposes.
 *
 * Fields:
 * - eventId: A unique identifier for the specific event.
 * - timestamp: The ISO 8601 formatted timestamp indicating when the event occurred.
 * - dayNumber: Represents the associated day number for the event, relative to a timeline or campaign.
 * - eventType: The type of event, represented by the {@link EventType} enum.
 * - agent: The name or identifier of the agent that triggered the event.
 *
 * Explicit Payload Variables:
 * - feedback: Represents feedback provided as part of the audit trail.
 * - previousContent: Stores the content before the event occurred.
 * - newContent: Holds the updated content resulting from the event.
 * - reviewOutcome: The outcome of any review process related to the event.
 */
@Data
@Builder
public class AuditTrailViewDTO {
    private String eventId;
    private String timestamp;
    private Integer dayNumber;
    private EventType eventType;
    private String agent;

    // Explicitly extracted payload variables
    private String feedback;
    private String previousContent;
    private String newContent;
    private String reviewOutcome;

    /**
     * Formats the raw timestamp into a human-readable local time for the UI.
     */
    public String getFormattedTimestamp() {
        if (this.timestamp == null || this.timestamp.isBlank()) {
            return "";
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
            return formatter.format(Instant.parse(this.timestamp));
        } catch (Exception e) {
            // Fallback to the raw string if parsing fails
            return this.timestamp;
        }
    }
}
