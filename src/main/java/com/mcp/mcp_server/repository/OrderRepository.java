package com.mcp.mcp_server.repository;

import com.mcp.mcp_server.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders with employee and product details, sorted by order date descending.
     *
     * @return list of orders with related entities
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.employee LEFT JOIN FETCH o.product ORDER BY o.orderDate DESC")
    List<Order> findAllWithDetails();

    /**
     * Find all orders by employee ID.
     *
     * @param employeeId the employee ID
     * @return list of orders for that employee
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.employee LEFT JOIN FETCH o.product WHERE o.employee.id = :employeeId ORDER BY o.orderDate DESC")
    List<Order> findByEmployeeId(Long employeeId);

    /**
     * Find an order by ID with employee and product details.
     *
     * @param id the order ID
     * @return Optional containing the order with related entities if found
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.employee LEFT JOIN FETCH o.product WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(Long id);

    /**
     * Find all orders by product ID.
     *
     * @param productId the product ID
     * @return list of orders for that product
     */
    List<Order> findByProductId(Long productId);

    /**
     * Find all orders by status.
     *
     * @param status the order status
     * @return list of orders with that status
     */
    List<Order> findByStatus(Order.OrderStatus status);

    /**
     * Find all orders by status with details (employee and product).
     *
     * @param status the order status
     * @return list of orders with that status including related entities
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.employee LEFT JOIN FETCH o.product WHERE o.status = :status ORDER BY o.orderDate DESC")
    List<Order> findByStatusWithDetails(Order.OrderStatus status);

    /**
     * Find orders created within a date range.
     *
     * @param startDate the start date
     * @param endDate the end date
     * @return list of orders within the date range
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate ORDER BY o.orderDate DESC")
    List<Order> findOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculate total revenue for all orders.
     *
     * @return total revenue
     */
    @Query("SELECT SUM(o.totalPrice) FROM Order o")
    java.math.BigDecimal calculateTotalRevenue();

    /**
     * Get total revenue from delivered orders only.
     *
     * @return total revenue from delivered orders
     */
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED'")
    Double getTotalRevenue();

    /**
     * Find top-selling products by quantity ordered.
     *
     * @param limit the number of products to return
     * @return list of top-selling products
     */
    @Query(value = "SELECT p.id, p.name, SUM(o.quantity) as total_quantity FROM products p " +
            "INNER JOIN orders o ON p.id = o.product_id GROUP BY p.id ORDER BY total_quantity DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopSellingProducts(int limit);

    /**
     * Get top 5 selling products by default.
     *
     * @return list of top-selling products
     */
    @Query(value = "SELECT p.name, SUM(o.quantity) FROM products p " +
            "INNER JOIN orders o ON p.id = o.product_id GROUP BY p.id ORDER BY SUM(o.quantity) DESC LIMIT 5",
            nativeQuery = true)
    List<Object[]> getTopSellingProducts();
}


