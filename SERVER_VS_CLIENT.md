# MCP Server vs Client Architecture

## Clear Separation of Concerns

### This Repository: MCP SERVER ✅

```
mcp-server/  (This is a SERVER repository)
├── Java Spring Boot application
├── Exposes MCP tools via HTTP
├── Runs on port 8080
└── Waits for external clients to call it
```

**What it does:**
1. Listens for MCP protocol requests
2. Exposes recruitment tools (parseJobDescription, ranking, etc.)
3. Uses Claude Haiku to analyze jobs and candidates
4. Returns results to calling clients

**What it does NOT do:**
- ❌ Does not connect to other services as an MCP client
- ❌ Does not implement MCP client protocol
- ❌ Does not reach out to remote MCP servers
- ❌ Does not consume tools from other MCP servers

### Separate: MCP CLIENT (To Be Implemented)

```
mcp-client/ (Future separate repository or module)
├── Node.js / Python / JavaScript / etc.
├── Connects to MCP servers (including this one)
├── Calls tools exposed by servers
└── Uses results in application logic
```

**What a client does:**
1. Initiates connection to MCP server
2. Requests list of available tools
3. Calls specific tools with parameters
4. Processes responses
5. Displays results or uses them in application

## Protocol Communication Flow

```
┌──────────────────┐
│   MCP CLIENT     │
│  (Your App)      │
└────────┬─────────┘
         │ Initiates HTTP connection
         │ Sends: "Give me available tools"
         │
         ▼
┌──────────────────────────────┐
│    THIS MCP SERVER           │
│  (mcp-server repository)     │
│                              │
│  Responds with:              │
│  - parseJobDescription       │
│  - searchCandidatesInZohoRecruit
│  - findAndRankCandidatesForJD
│  - ... (other tools)         │
└──────────────────────────────┘
         ▲
         │ Client calls: "Call findAndRankCandidatesForJD with JD: {..."
         │ Server returns: { success: true, rankedCandidates: [...] }
         │
┌────────┴─────────┐
│   MCP CLIENT     │
│  (Uses results)  │
└──────────────────┘
```

## Dependency Perspective

### Server Dependencies (pom.xml) ✅
```xml
<!-- Server Framework -->
spring-ai-starter-mcp-server-webmvc  ← Listens for MCP requests

<!-- Not present -->
<!-- spring-ai-starter-mcp-client -->  ← Would make this a client
```

### Client Dependencies (For Future Implementation) 📋
```javascript
// Example package.json (for separate client)
{
  "dependencies": {
    "@anthropic-ai/sdk": "^1.x",  ← Client protocol support
    "@modelcontextprotocol/sdk": "^0.x"  ← MCP client SDK
  }
}
```

## Key Distinctions

| Aspect | Server (This Repo) | Client (Separate) |
|--------|------|------|
| **Purpose** | Expose tools | Consume tools |
| **Initiates Connection** | ❌ No | ✅ Yes |
| **Listens on Port** | ✅ Yes (8080) | ❌ No |
| **Has MCP Server Dependency** | ✅ Yes | ❌ No |
| **Has MCP Client Dependency** | ❌ No | ✅ Yes |
| **Implements Tool Logic** | ✅ Yes | ❌ No |
| **Calls Tools** | ❌ No | ✅ Yes |
| **Uses Claude Haiku** | ✅ Yes (for analysis) | ❌ No |
| **Calls Zoho API** | ✅ Yes | ❌ No |

## How They Work Together

### Scenario: Rank candidates for a job

**Step 1: Server Ready**
```
Server started: ./mvnw spring-boot:run
→ Listening on http://localhost:8080
→ Tools available: parseJobDescription, findAndRankCandidatesForJD, etc.
```

**Step 2: Client Connects**
```javascript
// Client code (in separate app)
const client = new McpClient('http://localhost:8080');
const tools = await client.listTools();
console.log(tools); // [parseJobDescription, findAndRankCandidatesForJD, ...]
```

**Step 3: Client Calls Tool**
```javascript
// Client calls server tool
const result = await client.call('findAndRankCandidatesForJD', {
  jobDescription: 'Senior Java Developer with 5+ years...'
});
```

**Step 4: Server Processes**
```
Server receives request
→ Parses job description using Claude
→ Searches Zoho for candidates
→ Ranks candidates using Claude
→ Returns structured response to client
```

**Step 5: Client Uses Results**
```javascript
// Client processes response
console.log(`Found ${result.totalCandidatesEvaluated} candidates`);
result.rankedCandidates.forEach(candidate => {
  console.log(`${candidate.name}: ${candidate.matchPercentage}`);
});
```

## Single Server, Multiple Clients

The server can serve multiple clients simultaneously:

```
                    ┌─ Client 1: Claude.ai UI
                    │
Recruitment MCP ────├─ Client 2: Custom Web App
Server (8080)       │
                    └─ Client 3: Slack Bot
```

Each client independently connects and calls tools.

## Why This Architecture?

### ✅ Benefits of Pure Server

1. **Separation of Concerns**
   - Server handles data access and AI analysis
   - Clients handle presentation and user interaction

2. **Reusability**
   - One server, many clients
   - Clients can use different UIs/platforms

3. **Scalability**
   - Server handles heavy lifting (Claude API calls)
   - Clients are lightweight
   - Server can be deployed once, used by many

4. **Testing**
   - Server logic tested independently
   - Clients tested with mock server
   - Clear boundaries

5. **Maintainability**
   - Focused responsibility
   - Easy to find and fix bugs
   - Changes to one don't break the other

## Implementation Timeline

### Phase 1: MCP Server ✅ (Currently Complete)
- ✅ Pure Java Spring Boot server
- ✅ Exposes recruitment tools
- ✅ Uses Claude Haiku for analysis
- ✅ Integrates with Zoho Recruit

### Phase 2: MCP Client (To Be Implemented)
- 📋 Node.js / Python / JavaScript client
- 📋 Connects to this server
- 📋 UI/API to call tools
- 📋 Processes and displays results

## Configuration for Multi-Client Use

### Server Configuration (Already Set)
```yaml
spring.ai.mcp.server:
  type: ASYNC           # ← Handles concurrent clients
  protocol: streamable  # ← HTTP streaming (supports multiple connections)
```

### Client Configuration (When Implementing)
```yaml
# Each client will configure:
mcp:
  server:
    url: http://localhost:8080
    timeout: 30s
    retries: 3
```

## No Cross-Dependencies

```
mcp-server/            
├── NO imports from client code ✅
├── NO client dependencies ✅
└── CLEAN server-only ✅

mcp-client/ (when created)
├── Imports from server code ❌ (NO!)
├── Server artifacts/jars ❌ (NO!)
├── Only connects via HTTP protocol ✅
└── CLEAN client-only ✅
```

## Summary

### Server Role
This repository (`mcp-server`) is a **pure MCP server**:
- Runs as a service
- Exposes tools via MCP protocol
- Waits for clients to call it
- No client code or dependencies

### Client Role (Future)
Separate repository/module will be an **MCP client**:
- Connects to this server
- Calls exposed tools
- Uses results in application
- No server code or dependencies

### Key Takeaway
✅ **Clear separation**: Server ≠ Client
✅ **Communication**: HTTP MCP protocol only
✅ **Architecture**: Clean, maintainable, scalable

