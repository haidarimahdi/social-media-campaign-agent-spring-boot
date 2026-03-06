package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.DailyPost;
import com.example.socialmediacampaignagentsprintboot.model.PublishedPostAudit;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockSocialMediaService {

    private final ObjectMapper objectMapper;

    @Value("${publish.file.directory:./publish_logs}")
    private String baseDirectory;

    private static final String PUBLISH_LOG_FILE = "mock_published_posts.jsonl";

    public String publishToPlatform(String campaignId, DailyPost post) {

        // Simulate success
        String mockPostId = "PUB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String timestamp = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);

        // Log to console to see it happening in real-time
        log.info("[MOCK API] Publishing Day {} to {}...", post.getDayNumber(), post.getPlatform());

        PublishedPostAudit auditRecord = new PublishedPostAudit(
                mockPostId,
                campaignId,
                post.getDayNumber(),
                timestamp,
                post.getPlatform().name(),
                post.getContentPillar().name(),
                post.getFunnelStage().name(),
                post.getTargetAudience(),
                post.getGeneratedContent()
        );

        Path publishFilePath = Paths.get(baseDirectory, PUBLISH_LOG_FILE);

        try {
            Files.createDirectories(publishFilePath.getParent());

            String jsonLine = objectMapper.writeValueAsString(auditRecord) + System.lineSeparator();

            try (FileChannel channel = FileChannel.open(publishFilePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE)) {

                channel.write(ByteBuffer.wrap(jsonLine.getBytes(StandardCharsets.UTF_8)));
            }

            log.info("[MOCK API] Audit record appended to {}", publishFilePath);
        } catch (IOException e) {
            log.error("[MOCK API] Failed to write JSON audit log: {}", e.getMessage());
            return "ERROR: Publishing failed.";
        }

        return "SUCCESS (ID: " + mockPostId + ")";
    }
}
