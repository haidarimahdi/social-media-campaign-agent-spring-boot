package com.example.socialmediacampaignagentsprintboot.agent;

import com.example.socialmediacampaignagentsprintboot.model.OrchestratorResponse;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Represents an Orchestrator AI Agent responsible for managing and coordinating
 * various stages of social media campaign workflows. This agent interacts with
 * other domain-specific agents to execute end-to-end operations, such as planning,
 * drafting, and revising campaigns.
 *<p>
 * Key Responsibilities:
 * - Plan campaigns based on user-defined goals.
 * - Manage the drafting process by generating, reviewing, and revising posts for all
 *   pending campaign days.
 * - Handle revisions based on human-provided feedback for specific posts within a campaign.
 *<p>
 * Methods:
 * - `planCampaign`: Takes a campaign identifier and the user's goal, then creates
 *   a campaign plan by calling the appropriate tools. Guarantees the successful
 *   execution of the tool before confirming the plan.
 * - `draftCampaign`: Executes the drafting pipeline for all pending days in a campaign.
 *   Manages the lifecycle of drafting, reviewing, and rewriting posts until all drafts
 *   are approved and saved.
 * - `handleHumanRevision`: Processes feedback from a human user for a specific rejected
 *   post, rewrites the draft to address the provided feedback, and ensures it is
 *   subsequently reviewed and saved.
 */
public interface OrchestratorAgent {

    @SystemMessage("""
        You are the Campaign Planning Orchestrator.
        Your ONLY job is to take a user's goal, evaluate it, and generate a plan.
        
        1. When the user gives a goal, YOU MUST call the 'createCampaignPlan' tool.
        2. Wait for the tool to return a SUCCESS message.
        3. NEVER return 'PLAN_READY' unless you have successfully executed the 'createCampaignPlan' tool first.
        """)
    OrchestratorResponse planCampaign(@MemoryId String campaignId, @UserMessage String userGoal);

    @SystemMessage("""
        You are the Campaign Drafting Orchestrator.
        Your job is to execute the drafting pipeline for all pending days.
        
        For each PENDING day, follow this sequence:
        1. Use 'draftPost'.
        2. Immediately use 'reviewPost'.
        3. If rejected, use 'rewriteRejectedPost' passing the feedback, then 'reviewPost' again.
        
        When all days are SAVED_AND_APPROVED, return status 'DRAFTS_READY'.
        """)
    OrchestratorResponse draftCampaign(@MemoryId String campaignId, @UserMessage String command);

    @SystemMessage("""
        You are the Campaign Revision Orchestrator.
        Your job is to process a human user's direct feedback on a specific post.
        
        1. Use 'rewriteRejectedPost' with the user's feedback.
        2. Use 'reviewPost' to evaluate and auto-save the new draft.
        3. STOP and return status 'DRAFTS_READY'.
        """)
    OrchestratorResponse handleHumanRevision(@MemoryId String campaignId, @UserMessage String feedbackContext);

}
