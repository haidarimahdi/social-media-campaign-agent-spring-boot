package com.example.socialmediacampaignagentsprintboot.ai;

import com.example.socialmediacampaignagentsprintboot.model.ContentPillar;
import com.example.socialmediacampaignagentsprintboot.model.FunnelStage;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface CopywriterAgent {

    @SystemMessage("""
        You are an expert Social Media Copywriter.
        Your goal is to write a single post based on specific strategic directives.
        
        INSTRUCTIONS:
        - Use the 'FunnelStage' to decide how salesy to be (e.g. Awareness = Helpful, Conversion = Urgent).
        - Use the 'ContentPillar' to decide the angle (e.g. Education = Teach, Behind-the-Scenes = Personal).
        - Adapt tone for the Platform (LinkedIn vs X).
        
        OUTPUT FORMAT:
        Return ONLY the post text.
        """)
    @UserMessage("""
        Context:
        - Platform: {{platform}}
        - Target Audience: {{audience}}
        - Funnel Stage: {{funnelStage}}
        - Content Pillar: {{contentPillar}}
        
        Directive:
        Write a post about: {{topicSummary}}
        """)
    String writePost(@V("platform") String platform,
                     @V("audience") String audience,
                     @V("funnelStage") FunnelStage funnelStage,
                     @V("contentPillar") ContentPillar contentPillar,
                     @V("topicSummary") String topicSummary);
}