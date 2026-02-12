package com.example.socialmediacampaignagentsprintboot.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MockSocialMediaService {

    public String publishToPlatform(String platform, String content) {
        if (!platform.equalsIgnoreCase("LinkedIn") && !platform.equalsIgnoreCase("X")) {
            return " ERROR: Platform '" + platform + "' not supported.";
        }

        // Simulate success
        String postId = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = java.time.LocalDateTime.now().toString();

        // Log to console to see it happening in real-time
        System.out.println("🚀 [MOCK API] Posting to " + platform.toUpperCase());
        System.out.println("   📝 Content: \"" + (content.length() > 50 ? content.substring(0, 50) + "..." : content) + "\"");
        System.out.println("   ✅ Success! ID: " + postId);

        return "✅ Published to " + platform + " at " + timestamp + " (ID: " + postId + ")";
    }
}
