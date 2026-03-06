package com.example.socialmediacampaignagentsprintboot.config;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * A listener implementation for handling events related to interactions
 * with a chat-based language model. This listener captures and logs
 * relevant information during the request, response, and error phases.
 * <p>
 * The implementation is particularly useful for debugging and monitoring
 * token usage in interactions with the Large Language Model (LLM).
 * <p>
 * This listener includes methods for:
 * - Logging the size of the request messages sent to the LLM.
 * - Logging detailed token usage statistics from the LLM response.
 * - Logging error information when an LLM request fails.
 * <p>
 * Dependencies:
 * - Uses SLF4J for logging.
 * - Marked as a Spring Component for dependency injection.
 * <p>
 * Implements:
 * - `ChatModelListener` to handle events related to LLM interactions.
 */
@Slf4j
@Component
public class TokenLoggingListener implements ChatModelListener {

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        log.debug("Sending request to LLM with {} messages", requestContext.request().messages().size());
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        TokenUsage usage = responseContext.response().tokenUsage();
        if (usage != null) {
            log.info("[LLM Token Usage] Input: {} | Output: {} | Total: {}",
                    usage.inputTokenCount(), usage.outputTokenCount(), usage.totalTokenCount());
        }
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        log.error("LLM Request failed: {}", errorContext.error().getMessage());
    }
}
