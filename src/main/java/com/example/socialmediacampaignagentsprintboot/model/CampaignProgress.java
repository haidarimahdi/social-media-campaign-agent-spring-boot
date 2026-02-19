package com.example.socialmediacampaignagentsprintboot.model;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CampaignProgress {

    private final Set<Integer> publishedDays = ConcurrentHashMap.newKeySet();

    public boolean isDayComplete(int dayNumber) {
        return publishedDays.contains(dayNumber);
    }

    public void markDayComplete(int dayNumber) {
        publishedDays.add(dayNumber);
    }
}
