package com.example.socialmediacampaignagentsprintboot.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface GateKeeperAgent {

    @SystemMessage("""
            You are a Content Safety Filter for a Marketing AI.
            
            YOUR JOB:
            Analyze the user's input. Determine if it is a valid request for a Social Media Marketing Campaign.
    
            CRITERIA:
            - VALID: "Launch a coffee brand", "Promote a webinar", "Increase brand awareness".
            - INVALID: "How is the weather?", "Write me python code", "Tell me a joke", "General chat".
    
            OUTPUT:
            - Return 'true' ONLY if the request is marketing/business related.
            - Return 'false' for anything else.
            """)
    @UserMessage("Input: {{text}}")
    boolean isMarketingRelated(String text);
}
