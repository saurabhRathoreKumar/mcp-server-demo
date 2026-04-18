package com.mcp.mcp_server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.mcp_server.config.ZohoRecruitConfig;
import com.mcp.mcp_server.entity.Candidate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Zoho Recruit API Service
 * Handles interactions with Zoho Recruit API for candidate searches and job operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZohoRecruitAPIService {

    private final ZohoRecruitConfig zohoRecruitConfig;
    private final ZohoRecruitOAuthService oauthService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Search for candidates based on criteria
     *
     * @param criteria Search criteria (e.g., "(Skill_Set:contains:Python)and(Experience_in_Years:greater_equal:5)")
     * @param page Page number (default 1)
     * @param pageSize Results per page (default 200)
     * @return List of candidates matching criteria
     */
    public List<Candidate> searchCandidates(String criteria, Integer page, Integer pageSize) {
        try {
            int pageNum = page != null ? page : zohoRecruitConfig.getDefaultPage();
            int pageLen = pageSize != null ? pageSize : zohoRecruitConfig.getPageSize();

            log.info("Searching candidates with criteria: {} (page: {}, size: {})", criteria, pageNum, pageLen);

            // Build API URL with search parameters
            String url = UriComponentsBuilder.fromUriString(zohoRecruitConfig.getApiBaseUrl())
                    .path(zohoRecruitConfig.getCandidatesEndpoint())
                    .queryParam("criteria", criteria)
                    .queryParam("page", pageNum)
                    .queryParam("per_page", pageLen)
                    .build()
                    .toUriString();

            log.debug("Zoho Recruit API URL: {}", url);

            // Make authenticated API request
            ResponseEntity<String> response = makeAuthenticatedRequest(url, HttpMethod.GET);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseCandidatesResponse(response.getBody());
            } else {
                log.error("API request failed with status: {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Failed to search candidates", e);
            throw new RuntimeException("Failed to search candidates in Zoho Recruit", e);
        }
    }

    /**
     * Search for candidates by skill
     * Example: searchCandidatesBySkill("Python", 5) for Python developers with 5+ years experience
     *
     * @param skill Required skill
     * @param yearsOfExperience Minimum years of experience
     * @return List of matching candidates
     */
    public List<Candidate> searchCandidatesBySkill(String skill, Integer yearsOfExperience) {
        String criteria = buildSkillCriteria(skill, yearsOfExperience);
        return searchCandidates(criteria, 1, null);
    }

    /**
     * Build search criteria for skill-based search
     */
    private String buildSkillCriteria(String skill, Integer yearsOfExperience) {
        if (yearsOfExperience != null && yearsOfExperience > 0) {
            return String.format("((Skill_Set:contains:%s)and(Experience_in_Years:greater_equal:%d))", skill, yearsOfExperience);
        } else {
            return String.format("(Skill_Set:contains:%s)", skill);
        }
    }

    /**
     * Fetch detailed candidate information
     *
     * @param candidateId Zoho Recruit candidate ID
     * @return Candidate details
     */
    public Candidate getCandidateDetails(String candidateId) {
        try {
            log.info("Fetching candidate details for ID: {}", candidateId);

            String url = UriComponentsBuilder.fromUriString(zohoRecruitConfig.getApiBaseUrl())
                    .path(zohoRecruitConfig.getCandidatesEndpoint() + "/" + candidateId)
                    .build()
                    .toUriString();

            ResponseEntity<String> response = makeAuthenticatedRequest(url, HttpMethod.GET);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseSingleCandidateResponse(response.getBody());
            } else {
                log.error("Failed to fetch candidate details: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to get candidate details", e);
            throw new RuntimeException("Failed to fetch candidate details from Zoho Recruit", e);
        }
    }

    /**
     * Parse candidates from API response
     */
    private List<Candidate> parseCandidatesResponse(String responseBody) {
        List<Candidate> candidates = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Handle both direct array response and paginated response format
            JsonNode dataNode = root.has("data") ? root.get("data") : root;

            if (dataNode.isArray()) {
                for (JsonNode candidateNode : dataNode) {
                    Candidate candidate = parseCandidateFromNode(candidateNode);
                    if (candidate != null) {
                        candidates.add(candidate);
                    }
                }
            }

            log.info("Parsed {} candidates from API response", candidates.size());
        } catch (Exception e) {
            log.error("Failed to parse candidates response", e);
        }
        return candidates;
    }

    /**
     * Parse single candidate from API response
     */
    private Candidate parseSingleCandidateResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode dataNode = root.has("data") ? root.get("data") : root;
            return parseCandidateFromNode(dataNode);
        } catch (Exception e) {
            log.error("Failed to parse single candidate response", e);
            return null;
        }
    }

    /**
     * Convert JsonNode to Candidate entity
     */
    private Candidate parseCandidateFromNode(JsonNode node) {
        try {
            String firstName = getTextValue(node, "first_name");
            String lastName = getTextValue(node, "last_name");
            String fullName = firstName != null && lastName != null ? firstName + " " + lastName :
                              firstName != null ? firstName : 
                              lastName != null ? lastName : getTextValue(node, "name");
            
            return Candidate.builder()
                    .id(getTextValue(node, "id"))
                    .firstName(firstName)
                    .lastName(lastName)
                    .name(fullName)
                    .candidateId(getTextValue(node, "id"))
                    .email(getTextValue(node, "email"))
                    .phone(getTextValue(node, "phone"))
                    .currentCompany(getTextValue(node, "current_company"))
                    .currentDesignation(getTextValue(node, "current_designation"))
                    .currentPosition(getTextValue(node, "current_position"))
                    .yearsOfExperience(getIntValue(node, "experience_in_years"))
                    .experience(getTextValue(node, "experience"))
                    .resumeUrl(getTextValue(node, "resume_url"))
                    .location(getTextValue(node, "location"))
                    .source(getTextValue(node, "source"))
                    .status(getTextValue(node, "status"))
                    .skills(getListValue(node, "skill_set"))
                    .qualifications(getListValue(node, "qualifications"))
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.warn("Failed to parse candidate node", e);
            return null;
        }
    }

    /**
     * Make authenticated HTTP request to Zoho Recruit API
     */
    private ResponseEntity<String> makeAuthenticatedRequest(String url, HttpMethod method) {
        String accessToken = oauthService.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, method, entity, String.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JSON parsing helper methods
    // ─────────────────────────────────────────────────────────────────────────

    private String getTextValue(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
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


