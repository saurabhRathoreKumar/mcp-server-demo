# Quick Reference: Pure MCP Server

## What You Have

✅ **Pure MCP Server** - Spring Boot application that exposes recruitment tools via MCP protocol
✅ **AI-Enhanced Services** - Claude Haiku powered candidate ranking and JD parsing
✅ **NO Client Dependencies** - Clean server-only architecture

## Starting the Server

### Prerequisites
```bash
# Set environment variables
export ANTHROPIC_API_KEY=sk-ant-xxxxx
export ZOHO_RECRUIT_CLIENT_ID=your_client_id
export ZOHO_RECRUIT_CLIENT_SECRET=your_secret
export ZOHO_RECRUIT_REFRESH_TOKEN=your_token
export ZOHO_RECRUIT_ORG_ID=your_org_id
```

### Build & Run
```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run
# OR
java -jar target/mcp-server-1.0.jar
```

### Verify Server
```bash
curl http://localhost:8080/health
# Response:
# {
#   "status": "UP",
#   "service": "mysql-mcp-server",
#   "timestamp": "2026-04-18T...",
#   "endpoints": { "mcp": "/mcp", "health": "/health" }
# }
```

## Available MCP Tools

Once running, clients can access these tools via MCP protocol:

| Tool | Purpose | Parameters |
|------|---------|------------|
| `parseJobDescription` | Parse JD into structured format | jobDescription |
| `searchCandidatesInZohoRecruit` | Find candidates matching JD | jobDescription, maxResults |
| `findAndRankCandidatesForJD` | End-to-end ranking pipeline | jobDescription, maxResults |
| `generateSearchFiltersFromJD` | Create search criteria from JD | jobDescription |
| `getDetailedCandidateFitAnalysis` | AI fit analysis for candidate | jobDescription, candidateName, currentPosition, experience, skills |
| `generateCustomInterviewQuestions` | Create interview questions | jobDescription, candidateName, currentPosition, experience, skills, questionCount |

## Server Configuration

### Key Settings (application.yaml)

```yaml
# Server Port
server.port: 8080

# MCP Protocol
spring.ai.mcp.server.type: ASYNC
spring.ai.mcp.server.protocol: streamable

# Claude Haiku LLM
spring.ai.openai.api-key: ${ANTHROPIC_API_KEY}
spring.ai.openai.base-url: https://api.anthropic.com/v1
spring.ai.openai.model.name: claude-3-5-haiku-20241022

# AI Features
recruitment.ai.enabled: true
recruitment.ai.use-semantic-matching: true
recruitment.ai.confidence-threshold: 0.65

# Zoho Recruit
zoho.recruit.client-id: ${ZOHO_RECRUIT_CLIENT_ID}
zoho.recruit.api-url: https://recruit.zoho.com/api/v2
```

## Project Structure

```
mcp-server/
├── src/main/java/com/mcp/mcp_server/
│   ├── McpServerApplication.java          # Main entry point
│   ├── config/
│   │   ├── AIConfiguration.java            # ChatClient, ObjectMapper setup
│   │   ├── McpServerConfig.java            # Register tools with MCP
│   │   └── HealthController.java           # Health endpoint
│   ├── service/
│   │   ├── AIEnhancedCandidateRankingService.java  ⭐
│   │   ├── AIEnhancedJobDescriptionService.java    ⭐
│   │   ├── CandidateRankingService.java            (fallback)
│   │   ├── JobDescriptionParsingService.java       (fallback)
│   │   └── ZohoRecruitService.java                 (ATS integration)
│   ├── tools/
│   │   └── RecruitmentTools.java           # MCP tool definitions
│   └── entity/
│       ├── Candidate.java
│       ├── JobDescription.java
│       └── RankedCandidate.java
├── src/main/resources/
│   └── application.yaml                    # Configuration
├── pom.xml                                 # Dependencies (server-only)
└── mcp-client/                             # (Empty - for separate client)
    ├── ai-recruitment-client.js
    └── README.md
```

## Key Components

### 1. AIEnhancedCandidateRankingService
Uses Claude Haiku to:
- Rank candidates using semantic analysis
- Provide match percentages
- Identify matched/missing skills
- Generate detailed fit analysis
- Create interview questions
- Falls back to regex-based ranking if AI fails

### 2. AIEnhancedJobDescriptionService
Uses Claude Haiku to:
- Parse job descriptions
- Extract job title, skills, requirements
- Identify experience levels
- Determine department, location
- Falls back to regex-based parsing if AI fails

### 3. RecruitmentTools
Exposes 6 MCP tools that use the above services:
- Tools are auto-registered with MCP server
- Each tool has detailed description for clients
- Tools return standard JSON responses

## Connecting an MCP Client

To connect an external MCP client (e.g., Claude.ai, custom app):

```javascript
// Example: Node.js MCP client
const { Client } = require('@anthropic-ai/sdk');

const client = new Client({
  apiKey: process.env.ANTHROPIC_API_KEY,
  // Configure MCP server
  mcpServers: {
    recruitment: {
      command: 'http',
      args: ['http://localhost:8080/mcp']
    }
  }
});

// Call a tool
const result = await client.tools.call({
  name: 'findAndRankCandidatesForJD',
  input: {
    jobDescription: 'Senior Java Developer...'
  }
});
```

## Dependencies

### Included (Server-Only)
- ✅ `spring-ai-starter-mcp-server-webmvc` - MCP protocol
- ✅ `spring-ai-openai-spring-boot-starter` - Claude Haiku
- ✅ `spring-boot-starter-web` - REST framework
- ✅ `jackson-databind` - JSON
- ✅ `lombok` - Code generation

### NOT Included
- ❌ MCP client libraries
- ❌ Client protocol implementations
- ❌ Any client-specific dependencies

## Environment Variables

```bash
# Required
ANTHROPIC_API_KEY=sk-ant-xxxxx

# Zoho Recruit (if using Zoho integration)
ZOHO_RECRUIT_CLIENT_ID=xxxxx
ZOHO_RECRUIT_CLIENT_SECRET=xxxxx
ZOHO_RECRUIT_REFRESH_TOKEN=xxxxx
ZOHO_RECRUIT_ORG_ID=xxxxx

# Optional
ANTHROPIC_API_BASE_URL=https://api.anthropic.com/v1
LLM_MODEL=claude-3-5-haiku-20241022
```

## Logs

Server logs are written to:
```
~/logs/mcp-server.log
```

Change log location in application.yaml:
```yaml
logging:
  file:
    name: /path/to/mcp-server.log
```

## Troubleshooting

### Server won't start
- Check Java 21+ is installed: `java --version`
- Check ports (default: 8080) are not in use
- Check environment variables are set

### Tools not available
- Verify server is running: `curl http://localhost:8080/health`
- Check MCP server logs
- Verify `@Tool` annotations in RecruitmentTools.java

### AI features not working
- Check `ANTHROPIC_API_KEY` is set
- Verify API key is valid
- Check `recruitment.ai.enabled=true` in config
- See application logs for errors

### Zoho integration issues
- Verify Zoho credentials are set
- Check API endpoints are accessible
- Review Zoho API documentation

## Useful Commands

```bash
# Build
./mvnw clean package

# Run tests
./mvnw test

# Run with specific port
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=9000"

# View help
./mvnw spring-boot:help

# Build jar
./mvnw clean package -DskipTests

# Run jar
java -jar target/mcp-server-1.0.jar

# Check if running
curl http://localhost:8080/health
```

## Additional Documentation

- **ARCHITECTURE.md** - Detailed architecture overview
- **PURE_MCP_SERVER_VERIFICATION.md** - Verification checklist
- **pom.xml** - Maven dependencies
- **application.yaml** - Configuration options

## Summary

You have a **pure MCP server** ready to use:
1. ✅ Build with `./mvnw clean package`
2. ✅ Set environment variables (ANTHROPIC_API_KEY, Zoho credentials)
3. ✅ Run with `./mvnw spring-boot:run`
4. ✅ Connect external MCP clients to `http://localhost:8080`
5. ✅ Call tools via MCP protocol

No client code included - keep it clean!

