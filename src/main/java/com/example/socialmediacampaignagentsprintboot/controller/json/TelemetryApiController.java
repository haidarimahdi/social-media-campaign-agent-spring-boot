package com.example.socialmediacampaignagentsprintboot.controller.json;

import com.example.socialmediacampaignagentsprintboot.service.SseTelemetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class TelemetryApiController {

    private final SseTelemetryService telemetryService;

    @GetMapping(value = "/api/telemetry/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTelemetry(@RequestParam String campaignId) {
        return telemetryService.subscribe(campaignId);
    }
}
