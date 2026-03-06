package com.example.socialmediacampaignagentsprintboot.service;

import com.example.socialmediacampaignagentsprintboot.dto.CampaignPlanDTO;
import com.example.socialmediacampaignagentsprintboot.dto.DailyPostDTO;
import com.example.socialmediacampaignagentsprintboot.model.CampaignPlan;
import com.example.socialmediacampaignagentsprintboot.model.DailyPost;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Service responsible for handling operations related to campaign planning.
 * This class provides methods to transform domain-level campaign data into
 * Data Transfer Objects (DTOs) for use in other layers of the application.
 * The transformation ensures the encapsulation of core campaign details,
 * as well as the associated daily posting schedule.
 * <p>
 * Key Responsibilities:
 * - Convert instances of {@link CampaignPlan} into {@link CampaignPlanDTO}.
 * - Transform individual {@link DailyPost} entities into {@link DailyPostDTO}.
 */
@Data
@RequiredArgsConstructor
@Service
public class CampaignPlanService {

    public CampaignPlanDTO toDto(CampaignPlan plan) {
        if (plan == null) {
            return null;
        }

        CampaignPlanDTO dto = new CampaignPlanDTO();
        dto.setCampaignName(plan.getCampaignName());
        dto.setTargetAudience(plan.getTargetAudience());
        dto.setMainGoal(plan.getMainGoal());

        if (plan.getSchedule() != null) {
            dto.setSchedule(plan.getSchedule().stream().map(this::toPostDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    private DailyPostDTO toPostDTO(DailyPost post) {
        if (post == null) {
            return null;
        }

        DailyPostDTO dto = new DailyPostDTO();
        dto.setDayNumber(post.getDayNumber());
        dto.setPlatform(post.getPlatform());
        dto.setFunnelStage(post.getFunnelStage());
        dto.setContentPillar(post.getContentPillar());
        dto.setTopicSummary(post.getTopicSummary());
        dto.setStatus(post.getStatus());
        dto.setGeneratedContent(post.getGeneratedContent());

        return dto;
    }
}
