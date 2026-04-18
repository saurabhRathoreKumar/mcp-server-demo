# Quick Start Guide - Recruitment MCP Server

## 🚀 5-Minute Setup

### Step 1: Set Environment Variables
```bash
export ZOHO_RECRUIT_CLIENT_ID="your-client-id"
export ZOHO_RECRUIT_CLIENT_SECRET="your-client-secret"
export ZOHO_RECRUIT_REFRESH_TOKEN="your-refresh-token"
export ZOHO_RECRUIT_ORG_ID="your-org-id"
```

### Step 2: Build
```bash
cd /Volumes/A/MCP/mcp-server
mvn clean package
```

### Step 3: Run
```bash
mvn spring-boot:run
```

Server runs on: `http://localhost:8080`

---

## 📋 Available Tools

### 1️⃣ Parse Job Description
```json
{
  "tool": "parseJobDescription",
  "parameters": {
    "jobDescription": "Senior Java Developer with 5+ years experience..."
  }
}
```

### 2️⃣ Search Candidates
```json
{
  "tool": "searchCandidatesInZohoRecruit",
  "parameters": {
    "jobDescription": "...",
    "maxResults": 50
  }
}
```

### 3️⃣ Find & Rank Candidates (Recommended)
```json
{
  "tool": "findAndRankCandidatesForJD",
  "parameters": {
    "jobDescription": "...",
    "maxResults": 20
  }
}
```

### 4️⃣ Generate Search Filters
```json
{
  "tool": "generateSearchFiltersFromJD",
  "parameters": {
    "jobDescription": "..."
  }
}
```

---

## 📁 Key Files

| File | Purpose |
|------|---------|
| `RecruitmentTools.java` | 4 MCP tool methods |
| `JobDescriptionParsingService.java` | Parses JD using LLM |
| `ZohoRecruitService.java` | Zoho API client |
| `CandidateRankingService.java` | Skill matching & ranking |
| `application.yaml` | Configuration |

---

## 🔧 Configuration

**application.yaml** customization:
```yaml
zoho:
  recruit:
    client-id: ${ZOHO_RECRUIT_CLIENT_ID}
    client-secret: ${ZOHO_RECRUIT_CLIENT_SECRET}
    refresh-token: ${ZOHO_RECRUIT_REFRESH_TOKEN}
    organization-id: ${ZOHO_RECRUIT_ORG_ID}
    api-url: https://recruit.zoho.com/api/v2
```

---

## 📊 Sample Output

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
      "matchedSkills": ["Java", "Spring", "AWS"],
      "missingSkills": ["Kubernetes"],
      "fitAnalysis": "Strong fit - has most required skills..."
    }
  ]
}
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| "OAuth token not configured" | Set environment variables |
| "No candidates found" | Check Zoho Recruit database, broaden filters |
| "LLM parsing fails" | Falls back to rule-based parsing automatically |
| Port 8080 in use | Change `server.port` in application.yaml |

---

## 📚 Documentation

- **Full Setup**: See `RECRUITMENT_TOOLS_SETUP.md`
- **Architecture**: See `IMPLEMENTATION_SUMMARY.md`
- **What's New**: See `CLEANUP_COMPLETE.md`

---

## ✅ Removed Legacy Code

All old database tools have been removed:
- ❌ ProductTools, EmployeeTools, OrderTools
- ❌ Database repositories
- ❌ MySQL connector
- ❌ Spring Data JPA

---

## 🎯 What You Get

✅ Intelligent job description parsing  
✅ Zoho Recruit candidate search  
✅ Semantic skill matching  
✅ Smart ranking (0-100%)  
✅ Explainable results  
✅ Recruiter-ready output  

---

## 📞 Support

Check logs:
```bash
tail -f ~/logs/mcp-server.log
```

---

**Ready to find your next great hire!** 🎉

