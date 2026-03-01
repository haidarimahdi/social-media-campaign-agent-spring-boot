package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.agent.CopywriterAgent;
import com.example.socialmediacampaignagentsprintboot.agent.ReviewerAgent;
import com.example.socialmediacampaignagentsprintboot.model.Platform;
import com.example.socialmediacampaignagentsprintboot.model.ReviewResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QualityAssuranceService {

    private final CopywriterAgent copywriterAgent;
    private final ReviewerAgent reviewerAgent;

    private final static int MAX_RETRIES = 3;

    public String reviewAndRefineContent(Platform platform, String content) {
        for (int i = 1; i <= MAX_RETRIES ; i++) {
            System.out.println("🔍 Reviewing draft (Attempt " + i + ")...");
            ReviewResult review = reviewerAgent.reviewPost(platform, content);

            if (review.isApproved()) {
                System.out.println("✅ Post approved by Reviewer Agent.");
                break;
            } else {
                System.out.println("❌ Post rejected. Feedback: " + review.feedback());
                if (i < MAX_RETRIES) {
                    System.out.println("✍️ Asking Copywriter to rewrite...");
                    content = copywriterAgent.rewritePost(platform, content, review.feedback());
                } else {
                    System.out.println("⚠️ Max retries reached. Proceeding with last available draft.");
                }
            }
        }
        return content;
    }
}
