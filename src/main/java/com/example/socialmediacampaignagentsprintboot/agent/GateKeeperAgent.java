package com.example.socialmediacampaignagentsprintboot.agent;

import dev.langchain4j.service.UserMessage;

/**
 * Represents a GateKeeper AI Agent tasked with identifying whether a given input text
 * is related to marketing or not. This agent helps in filtering content based on its
 * relevance to marketing-related topics or objectives.
 *<p>
 * Key Responsibilities:
 * - Analyzes input text to determine its association with marketing.
 * - Provides a boolean response indicating whether the content is classified as
 *   marketing-related.
 *<p>
 * Note:
 * The implementation utilizes a user message annotation to process the input text
 * dynamically and provide an evaluation.
 *<p>
 * Method:
 * - `isMarketingRelated`: Accepts a string input and returns a boolean value indicating
 *   whether the text is related to marketing.
 */
public interface GateKeeperAgent {

    @UserMessage("Input: {{text}}")
    boolean isMarketingRelated(String text);
}
