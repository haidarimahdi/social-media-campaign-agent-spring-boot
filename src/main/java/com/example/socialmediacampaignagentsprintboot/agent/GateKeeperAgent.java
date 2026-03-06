package com.example.socialmediacampaignagentsprintboot.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * Represents an AI agent responsible for filtering user inputs to determine their relevance
 * to marketing or business-related queries. This interface provides the functionality to analyze
 * inputs and validate whether they are appropriate for initiating or planning a social media
 * marketing campaign.
 *
 * Functional Responsibilities:
 * - Analyze user input and identify if it pertains to a business or marketing-related campaign.
 * - Enforce a strict filter to exclude non-marketing queries, such as general chat, weather updates,
 *   or unrelated technical/code-writing requests.
 *
 * Input Criteria:
 * - VALID: Inputs related to marketing or campaign requests. Examples include:
 *   "Launch a coffee brand", "Promote a webinar", "Increase brand awareness".
 * - INVALID: Inputs unrelated to marketing. Examples include:
 *   "How is the weather?", "Write me python code", "Tell me a joke", "General chat".
 *
 * Output Criteria:
 * - Returns `true` if the user input is determined to be marketing or business-related.
 * - Returns `false` for inputs determined to be outside the scope of marketing or business relevance.
 */
public interface GateKeeperAgent {

    @UserMessage("Input: {{text}}")
    boolean isMarketingRelated(String text);
}
