package com.example.socialmediacampaignagentsprintboot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseTelemetryService {

    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String campaignId) {
        // Create an emitter with a 10-minute timeout
        SseEmitter emitter = new SseEmitter(600_000L);

        emitters.computeIfAbsent(campaignId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // Remove the emitter when the connection is closed or times out
        emitter.onCompletion(() -> removeEmitter(campaignId, emitter));
        emitter.onTimeout(() -> removeEmitter(campaignId, emitter));
        emitter.onError((e) -> removeEmitter(campaignId, emitter));

        log.info("[TELEMETRY] UI Subscribed to SSE stream for Campaign: {}", campaignId);
        return emitter;
    }

    public void broadcast(String campaignId, String message) {
        List<SseEmitter> campaignEmitters = emitters.get(campaignId);
        if (campaignEmitters != null) {
            for (SseEmitter emitter : campaignEmitters) {
                try {
                    // Push the text string to the frontend
                    emitter.send(SseEmitter.event().name("telemetry").data(message));
                } catch (IOException e) {
                    emitter.complete();
                    removeEmitter(campaignId, emitter);
                }
            }
        }
    }

    private void removeEmitter(String campaignId, SseEmitter emitter) {
        List<SseEmitter> campaignEmitters = emitters.get(campaignId);
        if (campaignEmitters != null) {
            campaignEmitters.remove(emitter);
            if (campaignEmitters.isEmpty()) {
                emitters.remove(campaignId);
            }
        }
    }
}
