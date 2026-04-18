# Intelligent Recruitment Tools - Setup Guide

## Overview

This MCP server now includes an intelligent recruitment system that integrates with **Zoho Recruit ATS** to automatically find, rank, and match candidates against job descriptions.

## Architecture

### Core Components

#### 1. **RecruitmentTools.java** (MCP Tool Endpoints)
Located in `src/main/java/com/mcp/mcp_server/tools/`

Four main MCP tools available:

- **`parseJobDescription(jobDescription)`** - Task 1: JD Ingestion
  - Extracts structured metadata from job descriptions
  - Uses LLM for intelligent parsing with rule-based fallback
  - Returns: job title, skills, experience level, qualifications, department, location

- **`searchCandidatesInZohoRecruit(jobDescription, maxResults)`** - Task 2: LinkedIn/Zoho Search
  - Searches Zoho Recruit ATS for matching candidates
  - Generates intelligent search filters from parsed JD
  - Handles pagination for large result sets
  - Returns: candidate profiles with contact info (name, email, phone)

- **`findAndRankCandidatesForJD(jobDescription, maxResults)`** - Tasks 3 & 4: Complete Pipeline
  - End-to-end recruitment workflow
  - Ranks candidates by relevance score (0-100%)
  - Returns recruiter-ready ranked list with match analysis

- **`generateSearchFiltersFromJD(jobDescription)`** - Utility Tool
  - Generates Boolean search queries
  - Provides filter criteria for manual searches
  - Returns Zoho Recruit API filters

#### 2. **Services**

**JobDescriptionParsingService.java**
- Parses job descriptions using Spring AI ChatClient
- Extracts: skills, experience level, qualifications, responsibilities
- Fallback rule-based parsing using regex patterns
- Common skills database (Java, Python, AWS, Kubernetes, etc.)

**ZohoRecruitService.java**
- OAuth 2.0 authentication with Zoho Recruit
- REST API integration for candidate search
- Pagination support for large result sets
- Candidate profile mapping and data extraction

**CandidateRankingService.java**
- Semantic similarity matching of skills (Levenshtein distance)
- Experience level matching
- Relevance scoring algorithm:
  - Skill Match: 60% weight
  - Experience Match: 40% weight
- Generates match explanations and fit analysis

#### 3. **Entities**

- **JobDescription** - Parsed JD with extracted metadata
- **Candidate** - Candidate profile from Zoho Recruit
- **RankedCandidate** - Ranked candidate with match score and reasoning

## Setup Instructions

### 1. Configure Environment Variables

Set the following environment variables or update `application.yaml`:

```bash
export ZOHO_RECRUIT_CLIENT_ID="your-zoho-client-id"
export ZOHO_RECRUIT_CLIENT_SECRET="your-zoho-client-secret"
export ZOHO_RECRUIT_REFRESH_TOKEN="your-zoho-refresh-token"
export ZOHO_RECRUIT_ORG_ID="your-zoho-organization-id"
```

Or update `src/main/resources/application.yaml`:

```yaml
zoho:
  recruit:
    client-id: your-client-id
    client-secret: your-client-secret
    refresh-token: your-refresh-token
    organization-id: your-org-id
    api-url: https://recruit.zoho.com/api/v2
```

### 2. Obtain Zoho Recruit OAuth Credentials

1. Go to [Zoho Recruit Developer Console](https://recruit.zoho.com/recruit/settings/oauth)
2. Create a new OAuth application
3. Get your Client ID, Client Secret
4. Generate a Refresh Token using the OAuth flow
5. Find your Organization ID in Zoho Recruit admin settings

### 3. Configure Spring AI

Ensure your Spring AI ChatClient is configured. Update `application.yaml`:

```yaml
spring:
  ai:
    mcp:
      server:
        type: ASYNC
        name: recruitment-mcp-server
        version: 1.0.0
        protocol: streamable
```

### 4. Build and Run

```bash
# Build the project
mvn clean package

# Run the server
mvn spring-boot:run

# Server will be available at http://localhost:8080
```

## Usage Examples

### Example 1: Parse a Job Description

```json
{
  "tool": "parseJobDescription",
  "parameters": {
    "jobDescription": "We are looking for a Senior Java Developer with 5+ years of experience..."
  }
}
```

**Response:**
```json
{
  "success": true,
  "jobTitle": "Senior Java Developer",
  "experienceLevel": "Senior",
  "yearsOfExperience": 5,
  "requiredSkills": ["Java", "Spring", "REST API", "SQL"],
  "preferredSkills": ["Kubernetes", "Docker", "AWS"],
  "qualifications": ["Bachelor's in Computer Science"],
  "department": "Engineering",
  "location": "Remote"
}
```

### Example 2: Find and Rank Candidates for JD

```json
{
  "tool": "findAndRankCandidatesForJD",
  "parameters": {
    "jobDescription": "Senior Java Developer, 5+ years experience with Spring Boot and AWS...",
    "maxResults": 20
  }
}
```

**Response:**
```json
{
  "success": true,
  "jobTitle": "Senior Java Developer",
  "totalCandidatesEvaluated": 15,
  "rankedCandidates": [
    {
      "candidateId": "cand_001",
      "name": "John Smith",
      "email": "john@example.com",
      "phone": "+1-555-0123",
      "matchPercentage": "92.5%",
      "matchedSkills": ["Java", "Spring", "AWS", "REST API", "SQL"],
      "missingSkills": ["Kubernetes", "Docker"],
      "matchReasoning": "Skill Match: 83.3% (5/6 required skills matched). Experience Match: 100%. Matched skills: Java, Spring, AWS...",
      "fitAnalysis": "Strong fit - candidate has most required skills. Main gaps: Kubernetes, Docker. Has relevant experience."
    },
    {
      "candidateId": "cand_002",
      "name": "Jane Doe",
      "email": "jane@example.com",
      "phone": "+1-555-0456",
      "matchPercentage": "78.0%",
      ...
    }
  ]
}
```

### Example 3: Generate Search Filters

```json
{
  "tool": "generateSearchFiltersFromJD",
  "parameters": {
    "jobDescription": "Python Developer with Django and PostgreSQL..."
  }
}
```

**Response:**
```json
{
  "success": true,
  "jobTitle": "Python Developer",
  "primarySearchTerms": "Python, Django, PostgreSQL",
  "experienceLevelFilter": "Mid",
  "yearsOfExperienceFilter": 3,
  "locationFilter": "San Francisco",
  "booleanSearchQuery": "\"Python Developer\" OR Python OR Django OR PostgreSQL",
  "zohoRecruitFilters": {
    "designation": "Python Developer",
    "skills": "Python,Django,PostgreSQL",
    "experience_level": "Mid",
    "location": "San Francisco"
  }
}
```

## Technical Details

### Skill Matching Algorithm

The system uses **semantic similarity** with Levenshtein distance for fuzzy matching:
- Normalized skill names (remove special chars, lowercase)
- Similarity threshold: 0.8 (80%)
- Handles typos and variations (e.g., "C++" vs "cpp")

### Ranking Algorithm

**Overall Match Score = (Skill Match × 0.6) + (Experience Match × 0.4)**

**Skill Match Calculation:**
- Percentage of required skills matched by candidate
- 0-100% based on overlap

**Experience Match Calculation:**
- Perfect match or more: 100%
- 1 year less: 85%
- 2 years less: 70%
- 3 years less: 60%
- 4+ years less: 50%

### PII Handling

- Candidates' full profiles are only displayed to authorized recruiters
- Email and phone are included (internal recruiter use)
- Can be further restricted if needed
- Zoho Recruit OAuth ensures data security

## API Pagination

The system handles large result sets:
- Default page size: 20 candidates per API call
- Automatic pagination up to 100 candidates
- Configurable via maxResults parameter

## Error Handling

### Common Issues

1. **OAuth Token Expired**
   - Automatic refresh token rotation
   - No action needed

2. **Zoho API Rate Limiting**
   - Exponential backoff implemented
   - Check logs for rate limit details

3. **LLM Parse Failure**
   - Automatic fallback to rule-based parsing
   - Reduced accuracy but graceful degradation

4. **No Candidates Found**
   - Adjust search filters
   - Broaden skill requirements
   - Check Zoho Recruit database population

## Logging

Enable debug logging to see details:

```yaml
logging:
  level:
    com.mcp.mcp_server: DEBUG
    org.springframework.ai: DEBUG
```

Log locations:
- File: `~/logs/mcp-server.log`
- Console: Runtime output

## Performance Considerations

- **Candidate Search**: ~2-5 seconds (includes Zoho API calls)
- **Ranking 50 candidates**: ~1-2 seconds
- **JD Parsing**: ~3-5 seconds (LLM inference)
- **Caching**: Access tokens cached until expiry

## Future Enhancements

- [ ] Caching of candidate profiles
- [ ] Resume parsing and extraction
- [ ] Advanced ML-based ranking models
- [ ] Integration with LinkedIn/Indeed APIs
- [ ] Duplicate candidate detection
- [ ] Skill gap recommendations
- [ ] Automated candidate outreach

## Support

For issues or questions:
1. Check logs: `tail -f ~/logs/mcp-server.log`
2. Verify Zoho credentials are valid
3. Ensure Spring AI is properly configured
4. Review error messages in tool responses

