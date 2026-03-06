package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import com.example.socialmediacampaignagentsprintboot.model.CampaignProgress;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Working Memory Service for Campaign Agents
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
