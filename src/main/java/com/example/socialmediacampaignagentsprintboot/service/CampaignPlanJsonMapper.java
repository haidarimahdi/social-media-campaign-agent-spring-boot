package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CampaignPlanJsonMapper {

    private final ObjectMapper objectMapper;

    public CampaignPlan parsePlan(String planJson) throws Exception {
        return objectMapper.readValue(planJson, CampaignPlan.class);
    }

    public  String serializePlan(CampaignPlan plan) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(plan);
    }
}