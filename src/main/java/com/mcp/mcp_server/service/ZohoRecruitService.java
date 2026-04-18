package com.mcp.mcp_server.service;

import com.mcp.mcp_server.entity.Candidate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for integrating with ZohoRecruit API to search for candidate profiles.
 */
@Slf4j
@Service
public class ZohoRecruitService {

    @Value("${zoho.recruit.client-id:}")
    private String clientId;

    @Value("${zoho.recruit.client-secret:}")
    private String clientSecret;

    @Value("${zoho.recruit.refresh-token:}")
    private String refreshToken;

    @Value("${zoho.recruit.api-url:https://recruit.zoho.com/api/v2}")
    private String apiUrl;

    @Value("${zoho.recruit.organization-id:}")
    private String organizationId;

    private final RestTemplate restTemplate;
    private String accessToken;

    public ZohoRecruitService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Authenticate with Zoho Recruit using OAuth and get access token
     */
    @SuppressWarnings("unchecked")
    public synchronized String getAccessToken() {
        if (accessToken != null && !accessToken.isEmpty()) {
            return accessToken;
        }

        if (refreshToken == null || refreshToken.isEmpty()) {
            log.error("Zoho Recruit refresh token not configured");
            throw new IllegalStateException("Zoho Recruit credentials not configured");
        }

        try {
            String tokenUrl = "https://accounts.zoho.com/oauth/v2/token";
            String body = String.format("grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s",
                    clientId, clientSecret, refreshToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(tokenUrl, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                accessToken = (String) response.getBody().get("access_token");
                log.debug("Successfully obtained Zoho Recruit access token");
                return accessToken;
            }
        } catch (Exception e) {
            log.error("Failed to authenticate with Zoho Recruit", e);
            throw new RuntimeException("Failed to authenticate with Zoho Recruit", e);
        }

        throw new RuntimeException("Failed to obtain Zoho Recruit access token");
    }

    /**
     * Search for candidates in Zoho Recruit using criteria
     *
     * @param searchCriteria Map of search criteria (e.g., skills, experience, location)
     * @param pageSize       Number of results per page
     * @return List of candidate profiles
     */
    public List<Candidate> searchCandidates(Map<String, String> searchCriteria, int pageSize) {
        List<Candidate> allCandidates = new ArrayList<>();
        int pageIndex = 1;
        boolean hasMore = true;

        while (hasMore && allCandidates.size() < 100) { // Limit to 100 candidates
            try {
                List<Candidate> pageCandidates = searchCandidatesPage(searchCriteria, pageIndex, pageSize);
                allCandidates.addAll(pageCandidates);

                // Check if we got fewer results than requested (indicates last page)
                if (pageCandidates.size() < pageSize) {
                    hasMore = false;
                }
                pageIndex++;
            } catch (Exception e) {
                log.error("Error fetching candidates page {}", pageIndex, e);
                hasMore = false;
            }
        }

        return allCandidates;
    }

    /**
     * Search for candidates in a specific page
     */
    @SuppressWarnings("unchecked")
    private List<Candidate> searchCandidatesPage(Map<String, String> searchCriteria, int pageIndex, int pageSize) {
        List<Candidate> candidates = new ArrayList<>();

        try {
            String token = getAccessToken();
            String endpoint = apiUrl + "/candidates";

            // Build query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endpoint)
                    .queryParam("index", pageIndex)
                    .queryParam("per_page", pageSize);

            // Add search criteria
            if (searchCriteria != null) {
                for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
                    builder.queryParam(entry.getKey(), entry.getValue());
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("X-com-zoho-organization", organizationId);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                List<Map<String, Object>> candidateList = (List<Map<String, Object>>) body.get("data");

                if (candidateList != null) {
                    candidates = candidateList.stream()
                            .map(this::mapToCandidate)
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.error("Error searching candidates in Zoho Recruit", e);
        }

        return candidates;
    }

    /**
     * Map Zoho API response to Candidate entity
     */
    private Candidate mapToCandidate(Map<String, Object> zohoProfile) {
        return Candidate.builder()
                .candidateId((String) zohoProfile.get("id"))
                .name((String) zohoProfile.get("first_name"))
                .email((String) zohoProfile.get("email"))
                .phone((String) zohoProfile.get("phone"))
                .location((String) zohoProfile.get("address"))
                .currentPosition((String) zohoProfile.get("current_position"))
                .experience((String) zohoProfile.get("experience"))
                .education((String) zohoProfile.get("education"))
                .zohoProfile(zohoProfile)
                .build();
    }

    /**
     * Get detailed candidate profile from Zoho Recruit
     */
    @SuppressWarnings("unchecked")
    public Candidate getCandidateDetails(String candidateId) {
        try {
            String token = getAccessToken();
            String endpoint = apiUrl + "/candidates/" + candidateId;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("X-com-zoho-organization", organizationId);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, (Class<Map<String, Object>>) (Class<?>) Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    return mapToCandidate(data);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching candidate details from Zoho Recruit", e);
        }

        return null;
    }
}


