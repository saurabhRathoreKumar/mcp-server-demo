package com.mcp.mcp_server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a ranked candidate with match score and reasoning
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankedCandidate {
    private String candidateId;
    private String name;
    private String email;
    private String phone;
    private Double matchPercentage;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String matchReasoning;
    private String fitAnalysis;
}

