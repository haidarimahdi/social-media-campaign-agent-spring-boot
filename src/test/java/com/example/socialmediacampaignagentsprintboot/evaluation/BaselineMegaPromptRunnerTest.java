package com.example.socialmediacampaignagentsprintboot.evaluation;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

@SpringBootTest
public class BaselineMegaPromptRunnerTest {

    @Value("${vertex.project-id}")
    private String projectId;

    @Value("${vertex.location}")
    private String location;

    @Test
    public void executeZeroShotBaseline() {
        System.out.println("Initializing Baseline Vertex AI Model (Temperature: 0.0)...");

        // 1. Initialize the baseline model with STRICT parameter parity (0.0f temperature)
        ChatLanguageModel baselineModel = VertexAiGeminiChatModel.builder()
                .project(projectId)
                .location(location)
                .modelName("gemini-2.5-flash")
                .temperature(0.0f)
                .maxOutputTokens(8192)
                .maxRetries(3)
                .build();

        // 2. Define the Zero-Shot Mega Prompt
        String megaPrompt = """
                You are an end-to-end Autonomous Marketing Campaign Generator for a Social Media Marketing Agency. Your objective is to validate the user's input, plan a strategic campaign, and write the final, publication-ready social media posts in one shot.
                
                === MODULE 1: GATEKEEPER (CONTENT SAFETY & VALIDATION) ===
                You are a Content Safety Filter for a Marketing AI.
                YOUR JOB:
                Analyze the user's input. Determine if it is a valid request for a social media campaign request in line with the brand voice.
                - If INVALID (non-marketing or inappropriate request), output a JSON object containing only: {"error": "Invalid request: Goal is not marketing-related."} and stop.

                === MODULE 2: PLANNER (STRATEGY & SCHEDULE) ===
                If the input request is VALID, generate a structured campaign plan based on the following rules:
                1. Create a SHORT, CONCISE campaign plan consisting of STRICTLY 4 Days.
                2. QUANTITY RESTRICTION: Generate EXACTLY ONE post per day. Do NOT create multiple posts for the same day.
                3. Keep descriptions brief.
                4. Assign a 'FunnelStage' (AWARENESS, CONSIDERATION, CONVERSION, or LOYALTY) to every post.
                5. Assign a 'ContentPillar' (EDUCATION, ENTERTAINMENT, PROMOTION, BEHIND_THE_SCENES, or USER_GENERATED_CONTENT) to every post.
                6. PLATFORM RESTRICTION: Use ONLY 'LINKEDIN' or 'X'. Do not use 'Instagram', 'TikTok', or 'Facebook'.

                === MODULE 3: COPYWRITER & QA REVIEWER (PLATFORM RULES) ===
                Draft and review the post content for each day, ensuring strict adherence to platform constraints:
                1. LinkedIn:
                   - Professional but engaging tone.
                   - Well-structured; use lists/bullet points if needed.
                   - Length: 500-1500 characters.
                2. X (Twitter):
                   - Casual, punchy tone.
                   - ABSOLUTE HARD LIMIT: 280 CHARACTERS MAX (including hashtags and spaces). Count characters strictly.
                   - PREFER: About 200 characters for better engagement.

                === MODULE 4: BRAND RULES ===
                	◦	We are "AquaCycle", a bold, efficiency-first brand disrupting household appliance waste.
                	◦	VOICE: Domestic, smart, eco-conscious.
                	◦	Keep the language practical, innovative, and clean. NEVER use words like: "loud", "wasteful", "scrubbing".
                	◦	Emojis can be used when relevant: 🍽️ 🌊 💧 ♻️
                	◦	NOTE: Free extended warranties are provided to eco-certified home builders to gather long-term usage metrics.
                	◦	NOTE: Offer free installation and 10% off for the first 5000 direct-to-consumer pre-orders to drive early adoption.
                	◦	Always include: Engineered to deliver spotless dishes using a fraction of the water.
                	◦	Focus messaging on closed-loop filtration, whisper-quiet operation, and energy efficiency.
                	◦	Call out common industry issues—pre-rinsing myths, noisy appliances, and hidden water waste.
                	
                === OUTPUT FORMAT ===
                Return ONLY a valid JSON object matching the following structure exactly.
                Do not include markdown code block syntax (```json), introductory text, or concluding remarks.

                {
                  "campaignName": "Short Catchy Campaign Name",
                  "targetAudience": "Target Demographic",
                  "mainGoal": "The input marketing goal",
                  "schedule": [
                    {
                      "dayNumber": 1,
                      "platform": "",
                      "funnelStage": "",
                      "contentPillar": "",
                      "topicSummary": "Brief topic summary",
                      "generatedContent": "Final post text adhering to all rules."
                    },
                    {
                      "dayNumber": 2,
                      "platform": "",
                      "funnelStage": "",
                      "contentPillar": "",
                      "topicSummary": "Brief topic summary",
                      "generatedContent": "Final post text adhering to all rules."
                    },
                    {
                      "dayNumber": 3,
                      "platform": "",
                      "funnelStage": "",
                      "contentPillar": "",
                      "topicSummary": "Brief topic summary",
                      "generatedContent": "Final post text adhering to all rules."
                    },
                    {
                      "dayNumber": 4,
                      "platform": "",
                      "funnelStage": "",
                      "contentPillar": "",
                      "topicSummary": "Brief topic summary",
                      "generatedContent": "Final post text adhering to all rules."
                    }
                  ]
                }
                """;

        // 3. Define the Test Dataset Goal
        String testGoal = "Drive pre-orders for a high-efficiency kitchen appliance that recycles its own rinse water.";

        System.out.println("Executing Mega Prompt for Goal: " + testGoal);

        // 4. Generate the baseline output
        String response = baselineModel.generate(
                SystemMessage.from(megaPrompt),
                UserMessage.from(testGoal)
        ).content().text();

        // 5. Save the output to a file for the Judge LLM to evaluate later
        saveBaselineOutput(testGoal, response);

        System.out.println("Baseline generation complete. Output saved to /debug_logs/");
    }

    /**
     * Utility method to write the baseline JSON output to a local file for later comparison.
     */
    private void saveBaselineOutput(String goal, String jsonResponse) {
        try {
            Path dirPath = Paths.get("debug_logs", "baseline");
            Files.createDirectories(dirPath);

            String filename = "baseline_output_" + Instant.now().toEpochMilli() + ".json";
            Path filePath = dirPath.resolve(filename);

            String logEntry = "{\n\"testGoal\": \"" + goal + "\",\n\"baselineOutput\": " + jsonResponse + "\n}";

            Files.writeString(filePath, logEntry, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save baseline output: " + e.getMessage());
        }
    }
}
