package com.example.socialmediacampaignagentsprintboot.agent;

import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DirectorAgent {

    String executeCampaign(String goal);

    @UserMessage("Execute this plan (ID: {{id}}): {{plan}}")
    String executeApprovedPlan(@V("id") String campaignId, @V("plan")CampaignPlan plan);

}
