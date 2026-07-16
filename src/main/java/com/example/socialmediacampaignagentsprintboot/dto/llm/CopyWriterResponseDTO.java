package com.example.socialmediacampaignagentsprintboot.dto.llm;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

/**
 * Data Transfer Object representing the response of a Copywriter module in a social media campaign system.
 * This object encapsulates the final text of the social media post,
 * along with any relevant hashtags and a description of the applied brand tone.
 *
 * @param postBody Final text of the social media post.
 * @param extractedHashtags List of hashtags extracted from the post.
 * @param appliedTone Description of the applied brand tone.
 */
public record CopyWriterResponseDTO(

        @Description("The final text of the social media post, adhering strictly to platform limits.")
        String postBody,

        @Description("A list of hashtags included in or relevant to the post.")
        List<String> extractedHashtags,

        @Description("A one-sentence explanation of how the brand tone (e.g., rebellious, witty) was applied.")
        String appliedTone
) {
}
