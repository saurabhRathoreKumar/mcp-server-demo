package com.mcp.mcp_server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.mcp_server.entity.JobDescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
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

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    /**
     * Parse job description using Claude Haiku AI
     */
    public JobDescription parseJobDescriptionWithAI(String jobDescription) {
        log.debug("Parsing job description using Claude Haiku AI");

        try {
            // Use Claude to intelligently extract JD metadata
            String aiAnalysis = analyzeJobDescriptionWithClaude(jobDescription);
            return buildJobDescriptionFromAIAnalysis(aiAnalysis, jobDescription);
        } catch (Exception e) {
            log.error("AI parsing failed", e);
            throw new RuntimeException("Failed to parse job description using AI", e);
        }
    }

    /**
     * Use Claude Haiku to analyze job description
     */
    private String analyzeJobDescriptionWithClaude(String jobDescription) {
        String prompt = """
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
                {jobDescription}
                """;

        String response = chatClient.prompt()
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
            log.error("Failed to parse AI response", e);
            throw new RuntimeException("Failed to parse AI response for job description", e);
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

