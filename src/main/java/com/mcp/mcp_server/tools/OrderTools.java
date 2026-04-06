package com.mcp.mcp_server.tools;

import com.mcp.mcp_server.entity.Order;
import com.mcp.mcp_server.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MCP Tools for querying the orders table.
 * Includes analytics tools like revenue totals and top-selling products.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderTools {

    private final OrderRepository orderRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 1 – list all orders (with employee & product details)
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Retrieves all orders with full details including the ordering employee's
            name and the product name. Results are sorted newest first.
            """)
    public List<Map<String, Object>> getAllOrders() {
        log.debug("MCP tool called: getAllOrders");
        return orderRepository.findAllWithDetails()
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 2 – get order by id
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = "Retrieves a single order by its numeric ID.")
    public Map<String, Object> getOrderById(
            @ToolParam(description = "The numeric ID of the order") Long id) {

        log.debug("MCP tool called: getOrderById({})", id);
        return orderRepository.findByIdWithDetails(id)
                .map(this::toMap)
                .orElse(Map.of("error", "Order not found with id: " + id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 3 – get orders by status
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Retrieves all orders that match the given status.
            Valid statuses: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED.
            """)
    public List<Map<String, Object>> getOrdersByStatus(
            @ToolParam(description = "Order status: PENDING, PROCESSING, SHIPPED, DELIVERED, or CANCELLED") String status) {

        log.debug("MCP tool called: getOrdersByStatus({})", status);
        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatusWithDetails(orderStatus)
                    .stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return List.of(Map.of("error",
                    "Invalid status '" + status + "'. Valid values: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 4 – get orders by employee
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = "Retrieves all orders placed by a specific employee, identified by their employee ID.")
    public List<Map<String, Object>> getOrdersByEmployee(
            @ToolParam(description = "The numeric ID of the employee") Long employeeId) {

        log.debug("MCP tool called: getOrdersByEmployee({})", employeeId);
        return orderRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 5 – analytics: total revenue
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Calculates the total revenue from all DELIVERED orders.
            Returns a single numeric value representing the sum of total_price
            for orders with status DELIVERED.
            """)
    public Map<String, Object> getTotalRevenue() {
        log.debug("MCP tool called: getTotalRevenue");
        Double revenue = orderRepository.getTotalRevenue();
        return Map.of(
                "totalRevenue", revenue != null ? revenue : 0.0,
                "currency", "USD",
                "note", "Calculated from DELIVERED orders only"
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 6 – analytics: top-selling products
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Returns the top-selling products ranked by total quantity ordered
            across all orders (regardless of status). Useful for sales analysis.
            """)
    public List<Map<String, Object>> getTopSellingProducts() {
        log.debug("MCP tool called: getTopSellingProducts");
        return orderRepository.getTopSellingProducts()
                .stream()
                .map(row -> Map.<String, Object>of(
                        "productName",   row[0],
                        "totalQuantity", row[1]
                ))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(Order o) {
        return Map.of(
                "id",           o.getId(),
                "employeeName", o.getEmployee() != null
                        ? o.getEmployee().getFirstName() + " " + o.getEmployee().getLastName()
                        : "N/A",
                "productName",  o.getProduct() != null ? o.getProduct().getName() : "N/A",
                "quantity",     o.getQuantity(),
                "totalPrice",   o.getTotalPrice() != null ? o.getTotalPrice() : 0,
                "orderDate",    o.getOrderDate() != null ? o.getOrderDate().toString() : "N/A",
                "status",       o.getStatus() != null ? o.getStatus().name() : "N/A"
        );
    }
}

