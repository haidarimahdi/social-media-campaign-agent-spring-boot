package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.StateTransitionEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Service responsible for managing the audit trail of campaign events. This service provides
 * methods to append events to the audit trail, read events from a specific campaign, and clear
 * the audit trail for a specific campaign.
 * <p>
 *     Features:
 *     - Automatically creates the audit trail directory if it doesn't exist.
 *     - Appends events to the audit trail in JSONL format.
 *     - Reads events from a specific campaign's audit trail.
 *     - Clears the audit trail for a specific campaign.
 *     - Synchronizes access to the audit trail file to prevent concurrent access.
 *     - Logs audit trail operations with detailed information.
 *     - Handles exceptions gracefully and logs errors.
 *     - Uses Jackson's ObjectMapper for JSON serialization and deserialization.
 *     - Uses a configurable base directory for audit trail storage.
 *     - Uses a UUID for event IDs if not provided.
 *     - Uses the current timestamp if not provided.
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditTrailService {

    private final ObjectMapper objectMapper;

    @Value("${audit.trail.directory:./audit_trails}")
    private String auditDirectory;

    private final Object fileLock = new Object();

    @PostConstruct
    public void init() {
        try {
            Path auditDir = Paths.get(auditDirectory);
            Files.createDirectories(auditDir);
            log.info("[AUDIT] Audit trail directory ready: {}", auditDir.toAbsolutePath());
        } catch (IOException e) {
            log.error("[AUDIT] Failed to create audit trail directory: {}", auditDirectory, e);
        }
    }

    public void appendEvent(StateTransitionEvent event) {
        if (event == null) {
            log.warn("[AUDIT] Skipping null event");
            return;
        }

        if (event.getEventId() == null || event.getEventId().isBlank()) {
            event.setEventId(java.util.UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null || event.getTimestamp().isBlank()) {
            event.setTimestamp(java.time.Instant.now().toString());
        }

        if (event.getCampaignId() == null || event.getCampaignId().isBlank()) {
            throw new IllegalArgumentException("Campaign ID must not be null or blank");
        }

        Path auditFile = resolveAuditFile(event.getCampaignId());

        synchronized (fileLock) {
            try {
                Files.createDirectories((auditFile.getParent()));

                ObjectWriter writer = objectMapper.writer();
                String jsonLine = writer.writeValueAsString(event);

                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(
                        auditFile, StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND)
                ) {
                    bufferedWriter.write(jsonLine);
                    bufferedWriter.newLine();
                }

                log.info("[AUDIT] Appended event {} for campaign {}", event.getEventType(), event.getEventId());
            } catch (IOException e) {
                log.error("[AUDIT] Failed to append event for campaign {}", event.getCampaignId(), e);
            }
        }
    }

    public List<StateTransitionEvent> readEvents(String campaignId) {
        Path auditFile = resolveAuditFile(campaignId);

        if (!Files.exists(auditFile)) {
            return List.of();
        }

        List<StateTransitionEvent> events = new ArrayList<>();

        synchronized (fileLock) {
            try (Stream<String> lines = Files.lines(auditFile)) {
                lines.filter(line -> line != null && !line.isBlank())
                        .forEach(line -> {
                            try {
                                StateTransitionEvent event =
                                        objectMapper.readValue(line, StateTransitionEvent.class);
                                events.add(event);
                            } catch (IOException e) {
                                log.error("[AUDIT] Failed to parse audit line for campaign {}", campaignId, e);
                            }
                        });
            } catch (IOException e) {
                log.error("[AUDIT] Failed to read audit trail for campaign {}", campaignId, e);
            }
        }

        return events;
    }

    public void clearAuditTrail(String campaignId) {
        Path auditFile = resolveAuditFile(campaignId);

        synchronized (fileLock) {
            try {
                Files.deleteIfExists(auditFile);
                log.info("[AUDIT] Cleared audit trail for campaign {}", campaignId);
            } catch (IOException e) {
                log.error("[AUDIT] Failed to clear audit trail for campaign {}", campaignId, e);
            }
        }
    }

    private Path resolveAuditFile(String campaignId) {
        return Paths.get(auditDirectory, campaignId + "-audit.jsonl");
    }

}
