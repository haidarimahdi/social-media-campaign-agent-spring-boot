package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileDebugService {

    private final ObjectMapper objectMapper;
    private static final String PLAN_FILE = "debug_latest_plan.json";
    private static final String POSTS_FILE = "debug_generated_posts.json";

    public void savePlan(CampaignPlan plan) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(PLAN_FILE), plan);
            System.out.println("💾 [DEBUG] Plan saved to " + PLAN_FILE);

            resetPostsLog();
        } catch (Exception e) {
                System.err.println("❌ Failed to save plan: " + e.getMessage());
        }
    }

    private void resetPostsLog() {
        try {
            Files.write(Paths.get(POSTS_FILE), "[]".getBytes());
        } catch (IOException e) {
            System.err.println("❌ Failed to reset posts debug file: " + e.getMessage());
        }
    }

    public synchronized void logPost(String platform, int dayNumber, String content) {
        try {
            File file = new File(POSTS_FILE);
            List<Map<String, Object>> posts;

            // Read existing posts
            if (file.exists() && file.length() > 0) {
                posts = objectMapper.readValue(file, List.class);
            } else {
                posts = new ArrayList<>();
            }

            // Add new entry
            Map<String, Object> entry = new HashMap<>();
            entry.put("day", dayNumber);
            entry.put("platform", platform);
            entry.put("content", content);
            entry.put("timestamp", LocalDateTime.now().toString());

            posts.add(entry);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, posts);
            System.out.println("💾 [DEBUG] post logged to " + POSTS_FILE);
        } catch (Exception e) {
            System.err.println("❌ Failed to log post: " + e.getMessage());
        }
    }
}
