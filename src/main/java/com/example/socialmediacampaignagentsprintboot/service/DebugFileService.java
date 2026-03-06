package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebugFileService {

    private final ObjectMapper objectMapper;

    @Value("${debug.file.directory:./debug_logs}")
    private String baseDirectory;

    private static final String PLAN_FILE = "debug_latest_plan.json";
    private static final String POSTS_FILE = "debug_generated_posts.json";

    private final Object fileLock = new Object();
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            log.error("[DEBUG] Failed to create debug directory: {}", baseDirectory, e);
        }
    }

    public void savePlan(CampaignPlan plan) {
        Path planPath = Paths.get(baseDirectory, PLAN_FILE);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(planPath.toFile(), plan);
            log.info("[DEBUG] Plan saved to {}", planPath);

            resetPostsLog();
        } catch (IOException e) {
            log.error("[DEBUG] Failed to save plan: {}", e.getMessage());
        }
    }

    private void resetPostsLog() {
        Path postsPath = Paths.get(baseDirectory, POSTS_FILE);
        try {
            Files.write(postsPath, "[]".getBytes());
        } catch (IOException e) {
            log.error("[DEBUG] Failed to reset posts debug file: {}", e.getMessage());
        }
    }

    public synchronized void logPost(String platform, int dayNumber, String content) {
        Path postsPath = Paths.get(baseDirectory, POSTS_FILE);
        File file = postsPath.toFile();

        Map<String, Object> entry = new HashMap<>();
        entry.put("day", dayNumber);
        entry.put("platform", platform);
        entry.put("content", content);
        entry.put("timestamp", LocalDateTime.now().toString());
        synchronized (fileLock) {
            try {
                List<Map<String, Object>> posts;

                // Read existing posts
                if (file.exists() && file.length() > 0) {
                    posts = objectMapper.readValue(file, new TypeReference<>() {
                    });
                } else {
                    posts = new ArrayList<>();
                }

                posts.add(entry);

                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, posts);
                log.info("[DEBUG] Post logged to {}", postsPath);
            } catch (IOException e) {
                log.error("[DEBUG] Failed to log post: {}", e.getMessage());
            }
        }
    }
}
