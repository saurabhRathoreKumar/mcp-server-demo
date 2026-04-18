# Intelligent Recruitment Tool - Problem Statement

## Description

Teams will build an intelligent tool that takes a Job Description as input and automatically searches Wissen's internal Zoho Recruit ATS to surface the most relevant existing candidate profiles. The output provides recruiter-ready contact information (name + phone/email)

---

## Core Deliverables

### Task 1 - JD Ingestion
Accept a JOB description as input. Parse and extract key skills, experience levels, role requirements, and qualifications automatically using an LLM.

**Objectives:**
- Ingest raw job description text
- Intelligently extract structured data:
  - Key skills (required and preferred)
  - Experience levels (Junior/Mid/Senior/Executive)
  - Role requirements and responsibilities
  - Qualifications and certifications
  - Department and location information
- Utilize LLM for intelligent analysis and extraction
- Handle various JD formats and structures

### Task 2 - Zoho Recruit Search
Integrate it with the ZohoRecruit (we have OAuth clientId, secret and other required info), to search for candidate profiles. Generate intelligent search filters/queries from the parsed JD to retrieve the most relevant profiles from the Zoho Recruit talent pool available.

**Objectives:**
- Authenticate with Zoho Recruit ATS using OAuth 2.0
- Generate intelligent search queries from parsed JD
- Create search filters that improve recall and precision
- Query the Zoho Recruit talent pool
- Handle API pagination for large result sets
- Return candidate profiles with available metadata

### Task 3 - Ranking and Scoring
Rank retrieved profiles by relevance against the JD. Provide a match percentage explaining why each candidate is a good or poor fit.

**Objectives:**
- Apply semantic similarity or keyword matching algorithms
- Compare candidate profiles against parsed JD requirements
- Calculate match percentage (0-100%)
- Rank candidates by relevance score
- Generate explanations for each match:
  - Matched skills
  - Missing skills
  - Experience level fit
  - Qualification alignment
- Provide detailed reasoning for good/poor fits

### Task 4 - Structured Output
Output a clean, ranked list: candidate name, phone/email, matched skills, and match percentage.

**Objectives:**
- Return recruiter-ready output format
- Include for each candidate:
  - Name
  - Email address
  - Phone number
  - Match percentage (0-100%)
  - Matched skills list
  - Missing skills list
  - Fit analysis summary
- Sort results by match percentage (highest first)
- Include explanations for each candidate's fit

---

## Technical Hints & Constraints

### 1. Use the ZohoRecruit APIs
- Utilize ZohoRecruit REST APIs for candidate search
- Implement OAuth 2.0 authentication with provided clientId, clientSecret, and refresh token
- Handle authentication token management and refresh
- Support secure API communication

### 2. LLM-Generated Search Queries/Filters (Encouraged)
- Generate intelligent search queries from parsed JD
- Create Boolean search operators for refined queries
- Build dynamic API filters based on extracted requirements
- Improve recall and precision through LLM-assisted filter generation
- Examples:
  - Primary search terms (job title, key skills)
  - Filter criteria (experience level, location, etc.)
  - Combined Boolean operators for complex queries

### 3. Apply Semantic Similarity or Keyword Matching to Rank Profiles
- Implement semantic similarity algorithms (e.g., Levenshtein distance, cosine similarity)
- Use keyword matching for skill comparison
- Handle skill name variations and abbreviations
- Compare:
  - Skills match
  - Experience level alignment
  - Qualification fit
  - Location match (if specified)
- Calculate weighted scoring if multiple criteria apply

### 4. Handle PII Responsibly
- Output is for internal recruiter use only
- Include email and phone in output (recruiter-facing)
- Implement access controls in production
- Document data usage and retention policies
- Ensure secure storage of candidate contact information
- Comply with data protection regulations (GDPR, etc.)

### 5. Output Must Include Match/Relevance Percentage for Each Candidate
- Calculate match percentage (0-100%)
- Display for every candidate in results
- Provide transparent calculation methodology
- Include breakdown of:
  - Skill match percentage
  - Experience match percentage
  - Overall relevance score
- Explain how percentage was calculated

### 6. Handle API Pagination
- Support paginated results from Zoho Recruit API
- Implement automatic pagination handling
- Configure page size appropriately
- Retrieve all relevant candidates (up to reasonable limit)
- Handle pagination errors gracefully
- Display pagination metadata (results shown, total available, etc.)

---

## Implementation Requirements

### Input
- Job Description: Raw text or structured format

### Processing
- Parse and extract metadata
- Generate search criteria
- Query Zoho Recruit ATS
- Rank and score candidates
- Generate explanations

### Output
```json
{
  "success": true,
  "jobTitle": "Senior Java Developer",
  "totalCandidatesEvaluated": 25,
  "rankedCandidates": [
    {
      "candidateId": "candidate_001",
      "name": "John Smith",
      "email": "john.smith@example.com",
      "phone": "+1-555-0123",
      "matchPercentage": 92.5,
      "matchedSkills": ["Java", "Spring", "AWS", "REST APIs"],
      "missingSkills": ["Kubernetes", "Docker"],
      "fitAnalysis": "Strong match - has all core skills and 8 years experience",
      "skillMatchPercentage": 80,
      "experienceMatchPercentage": 95,
      "rankPosition": 1
    },
    {
      "candidateId": "candidate_002",
      "name": "Jane Doe",
      "email": "jane.doe@example.com",
      "phone": "+1-555-0456",
      "matchPercentage": 78.0,
      "matchedSkills": ["Java", "Spring", "PostgreSQL"],
      "missingSkills": ["AWS", "REST APIs", "Kubernetes"],
      "fitAnalysis": "Good match - lacks cloud experience but strong in core Java",
      "skillMatchPercentage": 60,
      "experienceMatchPercentage": 90,
      "rankPosition": 2
    }
  ]
}
```

---

## Success Criteria

- ✅ JD parsing accurately extracts all key information
- ✅ Zoho Recruit API integration works with OAuth
- ✅ Search queries return relevant candidates
- ✅ Ranking algorithm produces accurate match percentages
- ✅ Output includes all required fields
- ✅ API pagination is handled correctly
- ✅ Response time is acceptable (<10 seconds for typical queries)
- ✅ PII is handled securely
- ✅ Error handling is robust
- ✅ Results are recruiter-ready and actionable

---

## Technology Stack Recommendations

- **LLM Integration:** OpenAI, Claude, or similar for JD parsing
- **API Framework:** Spring Boot, FastAPI, or Node.js
- **Authentication:** OAuth 2.0 for Zoho integration
- **Matching Algorithm:** Semantic similarity (Levenshtein distance, embeddings)
- **Database:** Optional caching layer for performance
- **Documentation:** API specification, setup guide, usage examples

---

## Dependencies & Prerequisites

1. Zoho Recruit ATS account with API access
2. OAuth credentials (clientId, clientSecret, refreshToken, organizationId)
3. LLM API access (if using external LLM)
4. Development environment with chosen tech stack
5. Network access to Zoho Recruit APIs

---

## Notes

- The tool should be production-ready with proper error handling
- Results should be sorted by match percentage (highest first)
- Consider implementing caching for frequently searched skills/candidates
- Provide clear logging for debugging and monitoring
- Include documentation for maintenance and updates
- Consider rate limiting for API calls
- Implement retry logic for transient failures

