package com.mcp.mcp_server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a candidate profile from ZohoRecruit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {
    private String candidateId;
    private String name;
    private String email;
    private String phone;
    private String location;
    private List<String> skills;
    private String currentPosition;
    private String experience;
    private Integer yearsOfExperience;
    private String education;
    private String resume;
    private Object zohoProfile; // Original Zoho API response
}

