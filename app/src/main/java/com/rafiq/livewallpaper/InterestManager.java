package com.rafiq.livewallpaper;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InterestManager {
    private static final String PREF_NAME = "UserInterests";
    private static final String KEY_INTERESTS = "interests";
    private SharedPreferences sharedPreferences;

    public InterestManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void addInterest(String tag) {
        if (tag == null || tag.isEmpty())
            return;

        Set<String> currentInterests = new HashSet<>(getInterests());
        currentInterests.add(tag.toLowerCase().trim());

        // Keep only last 10 interests to avoid huge query
        if (currentInterests.size() > 10) {
            // Simple removal of one item (not strictly LRU but sufficient)
            currentInterests.remove(currentInterests.iterator().next());
        }

        sharedPreferences.edit().putStringSet(KEY_INTERESTS, currentInterests).apply();
    }

    public Set<String> getInterests() {
        return sharedPreferences.getStringSet(KEY_INTERESTS, new HashSet<>());
    }

    public String getRecommendationQuery() {
        Set<String> interests = getInterests();
        if (interests.isEmpty())
            return "nature"; // Default fallback

        // Pick up to 3 random interests to form a query, or join all with OR logic
        // Pexels supports simple queries. Let's just join them with space or pick one.
        // Picking one random one is often better for variety.
        int size = interests.size();
        int item = new java.util.Random().nextInt(size);
        int i = 0;
        for (String interest : interests) {
            if (i == item)
                return interest;
            i++;
        }
        return "nature";
    }
}
