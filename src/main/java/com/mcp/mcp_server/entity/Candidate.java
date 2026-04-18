package com.mcp.mcp_server.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a candidate profile from Zoho Recruit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {
    private String id;                      // Zoho Recruit candidate ID
    private String candidateId;             // Alternative ID field
    private String firstName;
    private String lastName;
    private String name;                    // Full name
    private String email;
    private String phone;
    private String location;
    private String currentCompany;
    private String currentDesignation;
    private String currentPosition;         // Alternative position field
    private List<String> skills;
    private Integer yearsOfExperience;
    private String experience;              // Experience description
    private String education;
    private List<String> qualifications;    // Qualifications as list
    private String resume;
    private String resumeUrl;
    private String source;                  // Source (LinkedIn, Indeed, etc.)
    private String status;                  // Application status
    private LocalDateTime createdAt;
    private Object zohoProfile;             // Original Zoho API response
}

