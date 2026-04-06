package com.mcp.mcp_server.config;

import com.mcp.mcp_server.tools.EmployeeTools;
import com.mcp.mcp_server.tools.OrderTools;
import com.mcp.mcp_server.tools.ProductTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers all @Tool-annotated methods with the Spring AI MCP server.
 *
 * Each ToolCallbackProvider bean is picked up automatically by
 * spring-ai-starter-mcp-server-webmvc and advertised to MCP clients
 * in the tools/list response.
 */
@Configuration
public class McpServerConfig {

    /**
     * Registers all employee-related tools.
     * Tools exposed:
     *   - getAllEmployees
     *   - getEmployeeById
     *   - getEmployeesByDepartment
     *   - searchEmployeesByName
     *   - getAllDepartments
     *   - getEmployeesAboveSalary
     */
    @Bean
    public ToolCallbackProvider employeeToolProvider(EmployeeTools employeeTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(employeeTools)
                .build();
    }

    /**
     * Registers all product-related tools.
     * Tools exposed:
     *   - getAllProducts
     *   - getProductById
     *   - getProductBySku
     *   - searchProductsByName
     *   - getProductsByCategory
     *   - getLowStockProducts
     *   - getAllProductCategories
     */
    @Bean
    public ToolCallbackProvider productToolProvider(ProductTools productTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(productTools)
                .build();
    }

    /**
     * Registers all order-related tools.
     * Tools exposed:
     *   - getAllOrders
     *   - getOrderById
     *   - getOrdersByStatus
     *   - getOrdersByEmployee
     *   - getTotalRevenue
     *   - getTopSellingProducts
     */
    @Bean
    public ToolCallbackProvider orderToolProvider(OrderTools orderTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(orderTools)
                .build();
    }
}
