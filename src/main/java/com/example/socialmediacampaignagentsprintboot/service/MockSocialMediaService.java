package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.Platform;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class MockSocialMediaService {

    private static final String PUBLISH_LOG_FILE = "mock_published_posts.txt";

    public synchronized String publishToPlatform(Platform platform, String content) {

        // Simulate success
        String mockPostId = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Log to console to see it happening in real-time
        System.out.println("🚀 [MOCK API] Publishing to " + platform + "...");

        // Format the output for the text file
        String logEntry = String.format("""
                --------------------------------------------------
                TIMESTAMP: %s
                PLATFORM:  %s
                POST ID:   %s
                CONTENT:
                %s
                --------------------------------------------------
                
                """, timestamp, platform, mockPostId, content);

        try {
            Files.writeString(
                    Paths.get(PUBLISH_LOG_FILE),
                    logEntry,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            System.out.println("💾 [MOCK API] Post saved to " + PUBLISH_LOG_FILE);

        } catch (IOException e) {
            System.err.println("❌ [MOCK API] Failed to write to file: " + e.getMessage());
        }

        return "SUCCESS (ID: " + mockPostId + ")";
    }
}
