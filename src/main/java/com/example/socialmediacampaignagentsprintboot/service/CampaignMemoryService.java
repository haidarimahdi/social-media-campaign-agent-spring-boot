package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import com.example.socialmediacampaignagentsprintboot.model.CampaignProgress;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing campaign plans and progress in an in-memory store.
 * This service is useful for handling temporary data storage and retrieval
 * for social media campaigns during runtime.
 * <p>
 * Responsibilities:
 * - Stores and retrieves campaign plans using a unique campaign identifier.
 * - Tracks the progress of campaigns, including marking specific days as completed.
 * - Provides operations to update or get campaign data.
 */

@Slf4j
@RequiredArgsConstructor
@Service
public class CampaignMemoryService {

    private final ObjectMapper objectMapper;
    private static final String STATE_FILE_PATH = "campaign_state_backup.json";

    // In-memory storage
    @Getter
    private CampaignProgress currentCampaign;

    private final Map<String, CampaignPlan> planStore = new ConcurrentHashMap<>();
    private final Map<String, CampaignProgress> progressStore = new ConcurrentHashMap<>();

    /**
     * Initializes the service by loading the campaign state from the hard drive.
     * This method is called automatically when the service is initialized.
     */
    @PostConstruct
    public void init() {
        loadStateFromFile();
    }

    /**
     * Persists the current campaign state to a file in the hard drive.
     * This should be called when an agent finishes a task
     */
    public void persistStateToFile() {
//        if (currentCampaign == null) {
//            return;
//        }

        try {
            File file = new File(STATE_FILE_PATH);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, planStore);
            log.info("[STATE SAVED] Campaign progress successfully written to disk.");

        } catch (IOException e) {
            log.error("[STATE ERROR] Failed to write campaign state to disk.", e);
        }
    }

    /**
     * Reads the state from the hard drive into memory.
     *
     */
    public void loadStateFromFile() {
        File file = new File(STATE_FILE_PATH);
        if (file.exists()) {
            try {
                // Correctly reads the JSON file back into a Map of CampaignPlans
                Map<String, CampaignPlan> recoveredPlans = objectMapper.readValue(
                        file,
                        new TypeReference<Map<String, CampaignPlan>>() {}
                );

                // Repopulate the in-memory map
                planStore.putAll(recoveredPlans);
                log.info("[STATE RECOVERED] Campaign plans successfully loaded from disk. Count: {}", recoveredPlans.size());
            } catch (IOException e) {
                log.error("[STATE ERROR] Failed to read campaign state from file", e);
            }
        } else {
            log.info("[STATE CHECK] No previous state file found. Starting fresh.");
        }
    }

    /**
     * Helper method to grab a recovered plan to resume.
     * As a prototype, the first available plan in the store can be returned.
     */
    public CampaignPlan getActiveCampaignPlanForRecovery() {
        if (planStore.isEmpty()) {
            return null;
        }

        for (Map.Entry<String, CampaignPlan> entry : planStore.entrySet()) {
            CampaignPlan plan = entry.getValue();

            if (plan.getCampaignId() == null) {
                plan.setCampaignId(entry.getKey());
            }

            if (plan.getSchedule() != null && !plan.getSchedule().isEmpty()) {
                boolean hasUnfinishedWork = plan.getSchedule().stream()
                        .anyMatch(post ->
                                post.getStatus() == null ||
                                        "PENDING".equals(post.getStatus().name()) ||
                                        "FAILED".equals(post.getStatus().name())
                        );

                if (hasUnfinishedWork) {
                    log.info("[STATE SCAN] Found unfinished campaign: {}", plan.getCampaignId());
                    return plan;
                }
            }
        }

        log.warn("[STATE SCAN] No campaigns with PENDING or FAILED statuses were found.");
        return null;
    }

    public String savePlan(String campaignId, CampaignPlan plan) {
        planStore.put(campaignId, plan);
        progressStore.put(campaignId, new CampaignProgress());

        return campaignId;
    }

    public void updatePlan(String campaignId, CampaignPlan updatedPlan) {
        planStore.put(campaignId, updatedPlan);
    }

    public CampaignPlan getPlan(String campaignId) {
        return planStore.get(campaignId);
    }

    public void markPostAsPublished(String campaignId, int dayNumber) {
        progressStore.computeIfAbsent(campaignId, k -> new CampaignProgress())
                .markDayComplete(dayNumber);
    }

}
