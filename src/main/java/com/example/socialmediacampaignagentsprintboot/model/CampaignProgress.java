package com.example.socialmediacampaignagentsprintboot.model;

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
public class CampaignProgress {

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
