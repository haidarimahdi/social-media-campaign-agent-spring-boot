package com.example.socialmediacampaignagentsprintboot.config;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class
TokenLoggingListener implements ChatModelListener {

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
