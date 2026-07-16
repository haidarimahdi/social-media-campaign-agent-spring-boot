package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.StateTransitionEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Append-only audit trail writer that materialises the JSON State Contracts
 * required by FR1 and NFR1 of the Glass-Box architecture.
 * <p>
 * Design decisions:
 * - One file per campaign: {@code <baseDir>/audit_<campaignId>.json}.
 *   This keeps every campaign's full transition history self-contained and
 *   independently replayable by a human auditor or automated verifier.
 * - Append-only: events are never deleted or overwritten. A new event is
 *   appended on every call to {@link #record}.
 * - Thread-safe: a per-instance monitor lock guards the file read-modify-write
 *   cycle so concurrent drafting of different days does not corrupt the log.
 * - The {@code payload} field captures the full content snapshot at the moment
 *   of the transition (draft text, QA feedback, or plan JSON), ensuring that
 *   every intermediate artefact is recoverable even after subsequent rewrites.
 * <p>
 * The audit file is human-readable pretty-printed JSON for direct inspection
 * by operators without needing specialist tooling (EU AI Act Art. 14).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditTrailService {

    private final ObjectMapper objectMapper;

    @Value("${audit.trail.directory:./audit_trails}")
    private String baseDirectory;

    private final Object writeLock = new Object();

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(baseDirectory));
            log.info("[AUDIT] Audit trail directory ready: {}", baseDirectory);
        } catch (IOException e) {
            log.error("[AUDIT] Failed to create audit trail directory: {}", baseDirectory, e);
        }
    }

    /**
     * Records a state transition event to the campaign's append-only audit log.
     *
     * @param campaignId  The campaign this event belongs to.
     * @param dayNumber   The post day (use 0 for plan-level events).
     * @param agent       The agent or component triggering the transition
     *                    (e.g. "OrchestratorAgent", "ReviewerAgent", "HumanOperator").
     * @param fromStatus  The previous status, or null if this is the initial event.
     * @param toStatus    The new status after the transition.
     * @param payload     The full content snapshot at the moment of transition.
     *                    Pass an empty string when no content is relevant.
     */
    public void record(String campaignId,
                       int    dayNumber,
                       String agent,
                       String fromStatus,
                       String toStatus,
                       String payload) {

        StateTransitionEvent event = StateTransitionEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .campaignId(campaignId)
                .dayNumber(dayNumber)
                .agent(agent)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .payload(payload != null ? payload : "")
                .timestamp(Instant.now())
                .build();

        Path auditFile = Paths.get(baseDirectory, "audit_" + campaignId + ".json");

        synchronized (writeLock) {
            try {
                List<StateTransitionEvent> events = readExisting(auditFile.toFile());
                events.add(event);
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(auditFile.toFile(), events);
                log.info("[AUDIT] {} | day={} | {}→{} | agent={}",
                        campaignId, dayNumber, fromStatus, toStatus, agent);
            } catch (IOException e) {
                log.error("[AUDIT] Failed to write audit event for campaign {}: {}",
                        campaignId, e.getMessage());
            }
        }
    }

    /**
     * Returns the full ordered audit trail for a given campaign.
     * Returns an empty list if no audit file exists yet.
     *
     * @param campaignId The campaign whose trail to read.
     * @return Ordered list of {@link StateTransitionEvent} records.
     */
    public List<StateTransitionEvent> getTrail(String campaignId) {
        Path auditFile = Paths.get(baseDirectory, "audit_" + campaignId + ".json");
        synchronized (writeLock) {
            return readExisting(auditFile.toFile());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private List<StateTransitionEvent> readExisting(File file) {
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("[AUDIT] Failed to read existing audit file {}: {}", file.getName(), e.getMessage());
            return new ArrayList<>();
        }
    }
}
