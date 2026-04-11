# AGENTS.md – MCP Server Development Guide

This Spring Boot application implements a **Model Context Protocol (MCP) Server** exposing database queries as callable tools for AI assistants like Claude. Below is essential context for coding agents.

## Architecture Overview

**Three-layer design:**
- **Tools Layer** (`tools/*Tools.java`): @Tool-annotated methods auto-discovered by MCP framework
- **Repository Layer** (`repository/*.java`): JPA query interfaces; custom @Query for analytics
- **Entity/Config Layer** (`entity/*.java`, `config/*.java`): JPA entities and Spring AI MCP tool registration

**Data Flow:**
1. MCP client (e.g., Claude) connects via Streamable HTTP to `/sse` endpoint
2. Spring AI auto-discovers @Tool methods and advertises them in `tools/list` response
3. Tool invocation → Repository query → JSON serialization → MCP client response

**Key Pattern:** Each resource (Employee, Product, Order) follows identical structure:
- `Entity` with Lombok @Data and JPA annotations
- `Repository` extending JpaRepository with custom @Query methods
- `Tools` @Service with @Tool-annotated query methods that serialize results as Map<String, Object>
- `ToolCallbackProvider` bean in `McpServerConfig` registering all tools

## Critical Workflows

**Build & Test:**
```bash
./mvnw clean compile      # Incremental compile
./mvnw clean test         # All tests (requires mcp_demo_test DB)
./mvnw clean package -DskipTests
```

**Run Server:**
```bash
./mvnw spring-boot:run                    # Dev mode, port 8080
java -jar target/mcp-server-*.jar         # JAR mode
```

**Database Setup (once):**
```bash
mysql -u root -p < src/main/resources/static/sql/db.sql
```

**Verify Health:**
```bash
curl http://localhost:8080/actuator/health
curl -N http://localhost:8080/sse  # Test MCP SSE streaming
```

See `QUICK_COMMANDS.sh` for 70+ helper commands (coverage, logging, troubleshooting).

## Project-Specific Patterns

### Tool Design
- **Read-only transactions:** All @Tool methods use `@Transactional(readOnly=true)` for performance
- **Error handling:** Tool methods return `Map.of("error", message)` instead of throwing exceptions (MCP-client-safe)
- **Serialization:** Every entity has a `toMap(Entity)` helper converting to `Map<String, Object>` for JSON transport
- **Descriptive @ToolParam:** Each parameter includes `description` for Claude to understand what to pass

**Example from EmployeeTools:**
```java
@Tool(description = "Retrieves employees with salary >= threshold")
public List<Map<String, Object>> getEmployeesAboveSalary(
    @ToolParam(description = "Minimum salary, e.g. 90000") Double minSalary) { ... }
```

### Repository Queries
- Custom methods use Spring Data method naming (e.g., `findByDepartmentIgnoreCase`)
- Complex queries use `@Query("SELECT DISTINCT...")` for analytics (see `getAllDepartments`)
- Analytics tools in OrderTools use `findAllWithDetails()` to eager-load relationships and prevent N+1 queries

### Configuration & Deployment
- **JPA open-in-view: false** – Mandatory setting in `application.yaml` (line 17); prevents Hibernate lazy-loading hangs in async SSE threads
- **DDL mode: update** – Auto-creates schema; use `validate` in production
- **Spring AI MCP type: ASYNC** – Uses async thread pool for non-blocking MCP streaming
- Database: MySQL with seed data in `db.sql` (8 employees, 8 products, 10 orders)

## Integration Points

**Spring AI (v2.0.0-M4):**
- `@Tool` annotation marks public methods as MCP tools
- `ToolCallbackProvider` (via `MethodToolCallbackProvider`) registers tools
- `spring-ai-starter-mcp-server-webmvc` provides /sse endpoint + MCP protocol handling

**MySQL (Connector v9.3.0):**
- Connection URL: `jdbc:mysql://localhost:3306/mcp_demo`
- Credentials: root/root (dev-only, change in production)
- Foreign keys: orders.employee_id → employees.id, orders.product_id → products.id

**Lombok (v1.18.30):**
- Used on all entities: `@Data @NoArgsConstructor @AllArgsConstructor @Builder`
- Generates getters, setters, constructors, equals/hashCode

## Adding New Tools

**Checklist:**
1. Create/modify Entity in `entity/` with `@Entity`, `@Table`, Lombok annotations
2. Add Repository methods in `repository/` (extend JpaRepository, add custom queries if needed)
3. Create `*Tools.java` @Service with @Tool-annotated methods in `tools/`
4. Add `toMap(Entity)` helper returning `Map<String, Object>`
5. Register `ToolCallbackProvider` bean in `McpServerConfig`
6. Add seed data to `src/main/resources/static/sql/db.sql`

**Example (add a new Supplier tool):**
```java
// entity/Supplier.java
@Entity @Table(name = "suppliers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Supplier { @Id @GeneratedValue Long id; String name; ... }

// repository/SupplierRepository.java
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByNameContainingIgnoreCase(String name);
}

// tools/SupplierTools.java
@Slf4j @Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierTools {
    private final SupplierRepository repo;
    
    @Tool(description = "List all suppliers")
    public List<Map<String, Object>> getAllSuppliers() { ... }
    
    private Map<String, Object> toMap(Supplier s) { ... }
}

// config/McpServerConfig.java – add:
@Bean
public ToolCallbackProvider supplierToolProvider(SupplierTools tools) {
    return MethodToolCallbackProvider.builder().toolObjects(tools).build();
}
```

## Testing & Debugging

- Tests go in `src/test/java/com/mcp/mcp_server/` (currently no test files; add as needed)
- Test database: `mcp_demo_test` (separate from `mcp_demo`)
- Run tests with: `./mvnw clean test` (auto-creates schema if missing)
- Debug logs: Set `com.mcp.mcp_server` to DEBUG in `application.yaml` (already enabled)
- Log location: `~/logs/mcp-server.log` with 10MB rotation

## Important Notes

- **Java 21 required** (see `pom.xml` property)
- **Spring Boot 4.0.5** – Modern features, no Spring Security auto-config
- **Package name:** Uses `com.mcp.mcp_server` (not `com.mcp.mcp-server`; see HELP.md)
- **MCP protocol version:** 1.0.0, Streamable HTTP (legacy SSE replaced in newer versions)
- **No write operations exposed:** All Tools are read-only; MCP is query-only by design

