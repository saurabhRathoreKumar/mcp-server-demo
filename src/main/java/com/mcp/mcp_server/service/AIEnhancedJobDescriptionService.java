package com.mcp.mcp_server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.mcp_server.entity.JobDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI-Enhanced Job Description Parsing Service using Claude Haiku
 * Provides intelligent extraction of JD metadata using LLM analysis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIEnhancedJobDescriptionService {

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final ObjectMapper objectMapper;

    private ChatClient getChatClient() {
        try {
            ChatClient chatClient = chatClientProvider.getObject();
            if (chatClient == null) {
                log.error("ChatClient is null - AI service not properly initialized");
                throw new IllegalStateException("ChatClient not available. Ensure ANTHROPIC_API_KEY is set.");
            }
            return chatClient;
        } catch (Exception e) {
            log.error("Failed to get ChatClient: {}", e.getMessage(), e);
            throw new RuntimeException("ChatClient initialization failed. Please check if ANTHROPIC_API_KEY environment variable is set.", e);
        }
    }

    /**
     * Parse job description using Claude Haiku AI
     */
    public JobDescription parseJobDescriptionWithAI(String jobDescription) {
        log.debug("Parsing job description using Claude Haiku AI");

        try {
            // Verify API key is available
            String apiKey = System.getenv("ANTHROPIC_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("ANTHROPIC_API_KEY environment variable is not set");
                throw new IllegalStateException("ANTHROPIC_API_KEY environment variable is required but not set");
            }

            // Use Claude to intelligently extract JD metadata
            String aiAnalysis = analyzeJobDescriptionWithClaude(jobDescription);
            return buildJobDescriptionFromAIAnalysis(aiAnalysis, jobDescription);
        } catch (Exception e) {
            log.error("AI parsing failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse job description using AI: " + e.getMessage(), e);
        }
    }

    /**
     * Use Claude Haiku to analyze job description
     */
    private String analyzeJobDescriptionWithClaude(String jobDescription) {
        String prompt = String.format("""
                Analyze the following job description and extract structured metadata as JSON.
                Return ONLY a valid JSON object (no markdown, no extra text) with these exact fields:
                {
                  "jobTitle": "extracted job title",
                  "experienceLevel": "Junior/Mid/Senior/Executive",
                  "yearsOfExperience": number or null,
                  "requiredSkills": ["skill1", "skill2", "skill3", ...],
                  "preferredSkills": ["skill1", "skill2", ...],
                  "qualifications": ["qualification1", "qualification2", ...],
                  "responsibilities": ["responsibility1", "responsibility2", ...],
                  "department": "department name or null",
                  "location": "location or 'Remote' or null"
                }
                
                Be comprehensive and extract all relevant skills and requirements.
                
                Job Description:
                %s
                """, jobDescription);

         log.debug("Claude AI Analysis Prompt: {}", prompt);
         String response = getChatClient().prompt()
                 .user(prompt)
                 .call()
                 .content();

         log.debug("Claude AI Analysis Response: {}", response);
         return response;
    }

    /**
     * Build JobDescription entity from AI analysis
     */
    private JobDescription buildJobDescriptionFromAIAnalysis(String aiResponse, String originalJD) {
        try {
            // Clean the response (remove markdown code blocks if present)
            String cleanedResponse = aiResponse
                    .replaceAll("```json\\n?", "")
                    .replaceAll("```\\n?", "")
                    .trim();

            // Try to extract JSON if there's extra text (Claude might add explanation)
            // Look for the first { and last } to extract just the JSON
            int jsonStart = cleanedResponse.indexOf('{');
            int jsonEnd = cleanedResponse.lastIndexOf('}');

            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                cleanedResponse = cleanedResponse.substring(jsonStart, jsonEnd + 1);
                log.debug("Extracted JSON from response: {}", cleanedResponse);
            }

            JsonNode jsonNode = objectMapper.readTree(cleanedResponse);

            return JobDescription.builder()
                    .jobTitle(getTextValue(jsonNode, "jobTitle", "Not Specified"))
                    .jobDescription(originalJD)
                    .experienceLevel(getTextValue(jsonNode, "experienceLevel", "Mid"))
                    .yearsOfExperience(getIntValue(jsonNode, "yearsOfExperience"))
                    .requiredSkills(getListValue(jsonNode, "requiredSkills"))
                    .preferredSkills(getListValue(jsonNode, "preferredSkills"))
                    .qualifications(getListValue(jsonNode, "qualifications"))
                    .responsibilities(getListValue(jsonNode, "responsibilities"))
                    .department(getTextValue(jsonNode, "department", "Not Specified"))
                    .location(getTextValue(jsonNode, "location", "Not Specified"))
                    .build();
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", aiResponse, e);
            throw new RuntimeException("Failed to parse AI response for job description: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JSON helper methods
    // ─────────────────────────────────────────────────────────────────────────

    private String getTextValue(JsonNode node, String field, String defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return defaultValue;
    }

    private Integer getIntValue(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asInt();
        }
        return null;
    }

    private List<String> getListValue(JsonNode node, String field) {
        List<String> list = new ArrayList<>();
        if (node.has(field) && node.get(field).isArray()) {
            for (JsonNode item : node.get(field)) {
                list.add(item.asText());
            }
        }
        return list;
    }
}

