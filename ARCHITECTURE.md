# MCP Server Architecture

## Overview
This is a **Pure MCP Server** implementation focused exclusively on server-side AI-enhanced recruitment tools. It exposes recruitment-related tools via the Model Context Protocol (MCP) for consumption by separate MCP client instances.

## Key Components

### 1. MCP Server (Spring Boot)
**Package**: `com.mcp.mcp_server`

**Purpose**: Provides a streamable HTTP transport MCP server that exposes recruitment tools.

**Dependencies**:
- `spring-ai-starter-mcp-server-webmvc` - MCP server framework
- `spring-ai-openai-spring-boot-starter` - LLM integration (Claude via OpenAI-compatible API)
- Spring Boot Web (REST endpoints)

**Note**: NO MCP client dependencies are included in this module.

### 2. AI-Enhanced Services

#### AIEnhancedCandidateRankingService
- Ranks candidates using Claude Haiku AI
- Provides semantic matching beyond keyword matching
- Generates detailed fit analysis
- Creates interview questions
- Falls back to regex-based ranking if AI fails

#### AIEnhancedJobDescriptionService
- Parses job descriptions using Claude Haiku
- Extracts structured metadata (skills, experience level, responsibilities, etc.)
- Uses AI for intelligent extraction
- Falls back to regex-based parsing if AI fails

### 3. MCP Tools (Exposed via Tools/List)
**Package**: `com.mcp.mcp_server.tools`

The `RecruitmentTools` service exposes the following MCP tools:

1. **parseJobDescription** - Ingest and parse JD into structured format
2. **searchCandidatesInZohoRecruit** - Search candidates matching a job description
3. **findAndRankCandidatesForJD** - Complete end-to-end recruitment pipeline
4. **generateSearchFiltersFromJD** - Generate search queries from job description
5. **getDetailedCandidateFitAnalysis** - AI-powered fit analysis for a candidate
6. **generateCustomInterviewQuestions** - Generate targeted interview questions

### 4. Integration Points

#### Zoho Recruit ATS
- Integrates with Zoho Recruit for candidate database access
- Service: `ZohoRecruitService`
- Configuration: `zoho.recruit.*` in application.yaml

#### Claude Haiku (via Anthropic API)
- LLM for intelligent parsing and ranking
- Configuration: `spring.ai.openai.*` in application.yaml
- Uses OpenAI-compatible API endpoint with Anthropic

### 5. Server Configuration

**Application Configuration** (`application.yaml`):
```yaml
spring.ai.mcp.server:
  type: ASYNC
  protocol: streamable  # HTTP streaming transport
```

**MCP Server Registration** (`McpServerConfig`):
- Registers `RecruitmentTools` as a `ToolCallbackProvider`
- Automatically exposes all `@Tool`-annotated methods

## Architecture Diagram

```
┌─────────────────────────────────────────────┐
│         MCP Clients (External)              │
│  (To be implemented separately)             │
└────────────┬────────────────────────────────┘
             │ MCP Protocol (HTTP Streaming)
             │
┌────────────▼────────────────────────────────┐
│     Pure MCP Server (Spring Boot)           │
│  Port: 8080                                 │
├─────────────────────────────────────────────┤
│  RecruitmentTools (MCP Tools)               │
│  - parseJobDescription                      │
│  - searchCandidatesInZohoRecruit            │
│  - findAndRankCandidatesForJD               │
│  - generateSearchFiltersFromJD              │
│  - getDetailedCandidateFitAnalysis          │
│  - generateCustomInterviewQuestions         │
├─────────────────────────────────────────────┤
│  AI-Enhanced Services                       │
│  - AIEnhancedJobDescriptionService          │
│  - AIEnhancedCandidateRankingService        │
│  - CandidateRankingService (fallback)       │
│  - JobDescriptionParsingService (fallback)  │
├─────────────────────────────────────────────┤
│  External Integrations                      │
│  - Claude Haiku API (via OpenAI endpoint)   │
│  - Zoho Recruit API                         │
└─────────────────────────────────────────────┘
```

## Data Flow

### Job Description Parsing Pipeline
```
Job Description Text
    ↓
AIEnhancedJobDescriptionService
    ↓ (AI enabled?)
    ├→ Claude Haiku (parse via AI) → JobDescription entity
    └→ Regex fallback (if AI fails) → JobDescription entity
```

### Candidate Ranking Pipeline
```
Candidates + JobDescription
    ↓
AIEnhancedCandidateRankingService
    ↓ (AI enabled?)
    ├→ Claude Haiku (semantic ranking) → RankedCandidate list
    └→ Regex fallback (if AI fails) → RankedCandidate list
```

## Configuration Properties

### AI Configuration
```yaml
recruitment:
  ai:
    enabled: true                          # Enable/disable AI features
    use-semantic-matching: true            # Use semantic matching for skills
    confidence-threshold: 0.65             # Minimum confidence threshold
    max-skill-suggestions: 5               # Max skills to suggest
    enable-jd-optimization: true           # Enable JD optimization
    model-provider: anthropic-claude       # LLM provider
```

### LLM Provider Configuration
```yaml
spring.ai.openai:
  api-key: ${ANTHROPIC_API_KEY}
  base-url: https://api.anthropic.com/v1
  model:
    name: claude-3-5-haiku-20241022
```

### Server Configuration
```yaml
server.port: 8080
spring.ai.mcp.server.type: ASYNC
spring.ai.mcp.server.protocol: streamable
```

## Health Check
- Endpoint: `GET /health`
- Returns: Server status, service name, available endpoints

## Separate MCP Client Implementation

The `mcp-client/` folder is a placeholder for a separate MCP client implementation that will:
1. Connect to this MCP server via HTTP
2. Call the exposed MCP tools
3. Process responses and display results

**Note**: MCP client implementation is NOT included in this server module.

## Building and Running

### Build
```bash
./mvnw clean package
```

### Run
```bash
export ANTHROPIC_API_KEY=your_api_key
export ZOHO_RECRUIT_CLIENT_ID=your_client_id
export ZOHO_RECRUIT_CLIENT_SECRET=your_secret
./mvnw spring-boot:run
```

### Verify Server
```bash
curl http://localhost:8080/health
```

## Dependencies Summary

### Server Dependencies ✅
- `spring-boot-starter-web` - REST framework
- `spring-ai-starter-mcp-server-webmvc` - MCP server protocol
- `spring-ai-openai-spring-boot-starter` - Claude Haiku integration
- `jackson-databind` - JSON processing
- `lombok` - Code generation

### NOT Included ❌
- MCP client libraries
- External HTTP clients for MCP protocol
- Any client-side MCP dependencies

