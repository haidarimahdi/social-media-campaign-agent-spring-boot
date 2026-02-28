package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.agent.CopywriterAgent;
import com.example.socialmediacampaignagentsprintboot.agent.ReviewerAgent;
import com.example.socialmediacampaignagentsprintboot.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContentGraphService {

    private final CopywriterAgent copywriterAgent;
    private final ReviewerAgent reviewerAgent;
    private final static int MAX_RETRIES = 3;

    // --- 1. THE NODES (Isolated Agent Logic) ---

    private void copywriterNode(GraphState state) {
        state.setAttemptCount(state.getAttemptCount() + 1);
        System.out.println("🤖 [NODE: Copywriter] - Attempt " + state.getAttemptCount());

        if (state.getReviewerFeedback() == null) {
            // First time writing
            if (state.getHumanInstruction() != null) {
                state.setCurrentDraft(copywriterAgent.revisePost(state.getPlatform(), state.getCurrentDraft(), state.getHumanInstruction()));
            } else {
                state.setCurrentDraft(copywriterAgent.writePost(state.getPlatform(), state.getAudience(), state.getFunnelStage(), state.getContentPillar(), state.getTopicSummary()));
            }
        } else {
            // Rewriting based on QA feedback
            System.out.println("   -> Applying feedback: " + state.getReviewerFeedback());
            state.setCurrentDraft(copywriterAgent.rewritePost(state.getPlatform(), state.getCurrentDraft(), state.getReviewerFeedback()));
        }
    }

    private void reviewerNode(GraphState state) {
        System.out.println("🔍 [NODE: Reviewer] - Checking platform constraints...");
        ReviewResult result = reviewerAgent.reviewPost(state.getPlatform(), state.getCurrentDraft());
        state.setApproved(result.isApproved());
        state.setReviewerFeedback(result.feedback());
    }

    // --- 2. THE EDGES (Conditional Routing) ---

    private String determineNextNode(GraphState state) {
        if (state.isApproved()) {
            System.out.println("✅ [EDGE] Approved -> Routing to END");
            return "END";
        } else if (state.getAttemptCount() >= MAX_RETRIES) {
            System.out.println("⚠️ [EDGE] Max Retries Reached -> Routing to END");
            return "END";
        } else {
            System.out.println("❌ [EDGE] Rejected -> Routing back to COPYWRITER");
            return "COPYWRITER_NODE";
        }
    }

    // --- 3. THE GRAPH EXECUTOR ---

    public String executeNewPostGraph(Platform platform, String audience, FunnelStage stage, ContentPillar pillar, String topic) {
        GraphState state = new GraphState();
        state.setPlatform(platform);
        state.setAudience(audience);
        state.setFunnelStage(stage);
        state.setContentPillar(pillar);
        state.setTopicSummary(topic);

        return runGraph(state);
    }

    public String executeRevisionGraph(Platform platform, String currentDraft, String instruction) {
        GraphState state = new GraphState();
        state.setPlatform(platform);
        state.setCurrentDraft(currentDraft);
        state.setHumanInstruction(instruction);

        return runGraph(state);
    }

    private String runGraph(GraphState state) {
        String currentNode = "COPYWRITER_NODE"; // Entry Point

        while (!currentNode.equals("END")) {
            switch (currentNode) {
                case "COPYWRITER_NODE":
                    copywriterNode(state);
                    currentNode = "REVIEWER_NODE"; // Unconditional Edge
                    break;
                case "REVIEWER_NODE":
                    reviewerNode(state);
                    currentNode = determineNextNode(state); // Conditional Edge
                    break;
            }
        }
        return state.getCurrentDraft();
    }
}
