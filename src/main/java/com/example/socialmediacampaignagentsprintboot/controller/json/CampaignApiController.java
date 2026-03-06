package com.example.socialmediacampaignagentsprintboot.controller.json;

import com.example.socialmediacampaignagentsprintboot.service.CampaignWorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CampaignApiController {

    private final CampaignWorkflowService workflowService;

    @PostMapping("/publish-single")
    @ResponseBody
    public String publishSinglePost(@RequestParam String campaignId,
                                    @RequestParam int dayNumber,
                                    @RequestParam String content) {
        try {
            return workflowService.publishSinglePost(campaignId, dayNumber, content);
        } catch (Exception e) {
            return "Failed to publish Day " + dayNumber + ": " + e.getMessage();
        }
    }
}
