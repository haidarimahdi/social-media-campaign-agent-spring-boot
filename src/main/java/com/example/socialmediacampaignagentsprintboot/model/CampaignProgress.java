package com.example.socialmediacampaignagentsprintboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the progress of a social media campaign by tracking the days that have been
 * marked as drafted and completed.
 * <p>
 * This class provides methods to query and update the status of specific days in the campaign
 * timeline. The statuses tracked include:
 * - Drafted: Indicates that content for the day has been drafted but not yet published.
 * - Complete: Indicates that content for the day has been finalized and published.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class CampaignProgress {

    @Getter
    @Setter
    private String campaignId;

    @Getter
    @Setter
    private String goal;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private List<DailyPost> posts = new ArrayList<>();

    public CampaignProgress(String campaignId, String goal) {
        this.campaignId = campaignId;
        this.goal = goal;
        this.status = "INITIALIZED";
    }

    private final Set<Integer> draftedDays = ConcurrentHashMap.newKeySet();
    private final Set<Integer> publishedDays = ConcurrentHashMap.newKeySet();

    public boolean isDayDrafted(int dayNumber) {
        return draftedDays.contains(dayNumber);
    }

    public void markDayDrafted(int dayNumber) {
        draftedDays.add(dayNumber);
    }

    public boolean isDayComplete(int dayNumber) {
        return publishedDays.contains(dayNumber);
    }

    public void markDayComplete(int dayNumber) {
        publishedDays.add(dayNumber);
    }
}
