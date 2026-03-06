package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service responsible for mapping JSON data to and from the {@link CampaignPlan} object.
 * This class utilizes Jackson's {@link ObjectMapper} to handle JSON serialization
 * and deserialization with the given campaign plan model.
 * <p>
 * Responsibilities:
 * - Converts JSON strings to {@link CampaignPlan} objects.
 * - Converts {@link CampaignPlan} objects to JSON strings with a formatted output.
 * <p>
 * Dependencies:
 * - {@link ObjectMapper}: Automatically configured and injected to enable JSON operations.
 * <p>
 * Methods:
 * - {@link #parsePlan(String)}: Parses a JSON string into a {@link CampaignPlan} object.
 *   Throws an exception if the input JSON is invalid or deserialization fails.
 * - {@link #serializePlan(CampaignPlan)}: Serializes a {@link CampaignPlan} object into
 *   a formatted JSON string. Throws an exception if serialization fails.
 */
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