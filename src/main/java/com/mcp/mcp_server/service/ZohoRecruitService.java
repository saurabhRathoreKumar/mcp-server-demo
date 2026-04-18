package com.mcp.mcp_server.service;

import com.mcp.mcp_server.entity.Candidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for integrating with ZohoRecruit API to search for candidate profiles.
 *
 * This is a wrapper/facade around the new Zoho Recruit OAuth and API services
 * that maintains backward compatibility with existing code.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZohoRecruitService {

    private final ZohoRecruitAPIService zohoRecruitAPIService;
    private final ZohoRecruitOAuthService oauthService;

    /**
     * Search for candidates in Zoho Recruit using criteria
     *
     * @param searchCriteria Map of search criteria (e.g., skills, experience, location)
     * @param pageSize       Number of results per page
     * @return List of candidate profiles
     */
    public List<Candidate> searchCandidates(Map<String, String> searchCriteria, int pageSize) {
        try {
            log.info("Searching candidates with criteria: {}", searchCriteria);

            // Build criteria string from map for new API service
            String criteria = buildCriteriaString(searchCriteria);

            // Call the new API service
            return zohoRecruitAPIService.searchCandidates(criteria, 1, pageSize);
        } catch (Exception e) {
            log.error("Error searching candidates in Zoho Recruit", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get detailed candidate profile from Zoho Recruit
     */
    public Candidate getCandidateDetails(String candidateId) {
        try {
            log.info("Fetching candidate details for ID: {}", candidateId);
            return zohoRecruitAPIService.getCandidateDetails(candidateId);
        } catch (Exception e) {
            log.error("Error fetching candidate details from Zoho Recruit", e);
            return null;
        }
    }

    /**
     * Build Zoho Recruit search criteria string from map
     * Converts map like {"skills": "Java,Python", "experience_level": "Senior"}
     * to criteria string like "(Skill_Set:contains:Java)and(Experience_Level:contains:Senior)"
     */
    private String buildCriteriaString(Map<String, String> searchCriteria) {
        if (searchCriteria == null || searchCriteria.isEmpty()) {
            return "";
        }

        List<String> criteria = new ArrayList<>();

        for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.isEmpty()) {
                continue;
            }

            // Map common criteria keys to Zoho field names
            String zohoField = mapToZohoField(key);

            if ("skills".equals(key)) {
                // Handle multiple skills
                for (String skill : value.split(",")) {
                    criteria.add(String.format("(Skill_Set:contains:%s)", skill.trim()));
                }
            } else if ("experience_level".equals(key)) {
                criteria.add(String.format("(Experience_Level:contains:%s)", value));
            } else if ("location".equals(key)) {
                criteria.add(String.format("(Location:contains:%s)", value));
            } else if ("designation".equals(key) || "job_title".equals(key)) {
                criteria.add(String.format("(Designation:contains:%s)", value));
            } else {
                // Generic field mapping
                criteria.add(String.format("(%s:contains:%s)", zohoField, value));
            }
        }

        // Join criteria with 'and' operator
        if (criteria.isEmpty()) {
            return "";
        }

        return "(" + String.join(")and(", criteria) + ")";
    }

    /**
     * Map search criterion key to Zoho Recruit field name
     */
    private String mapToZohoField(String key) {
        return switch (key) {
            case "skills" -> "Skill_Set";
            case "experience_level" -> "Experience_Level";
            case "location" -> "Location";
            case "designation", "job_title" -> "Designation";
            case "company" -> "Company";
            case "email" -> "Email";
            case "phone" -> "Phone";
            default -> key.replaceAll("_", "_");
        };
    }

    /**
     * Get access token (for backward compatibility)
     */
    public synchronized String getAccessToken() {
        return oauthService.getAccessToken();
    }

    /**
     * Invalidate cached token to force refresh
     */
    public void invalidateToken() {
        oauthService.invalidateToken();
    }
}


