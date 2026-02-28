package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CopilotRevisionService {

    private final ContentGraphService graphService;

    public String reviseAndReviewPost(Platform platform, String currentDraft, String instruction) {
        System.out.println("✨ AI Co-Pilot initiating revision graph...");

        return graphService.executeRevisionGraph(platform, currentDraft, instruction);
    }
}
