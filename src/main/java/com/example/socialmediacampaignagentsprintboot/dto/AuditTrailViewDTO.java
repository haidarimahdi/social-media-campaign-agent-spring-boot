package com.example.socialmediacampaignagentsprintboot.dto;

import com.example.socialmediacampaignagentsprintboot.model.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
