package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import com.example.socialmediacampaignagentsprintboot.model.CampaignProgress;
import org.springframework.stereotype.Service;

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
@Service
public class CampaignMemoryService {

    // In-memory storage
    private final Map<String, CampaignPlan> planStore = new ConcurrentHashMap<>();
    private final Map<String, CampaignProgress> progressStore = new ConcurrentHashMap<>();


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
