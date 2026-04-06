package com.mcp.mcp_server.repository;

import com.mcp.mcp_server.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find a product by SKU (Stock Keeping Unit).
     *
     * @param sku the product SKU
     * @return Optional containing the product if found
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find a product by SKU (case-insensitive).
     *
     * @param sku the product SKU
     * @return Optional containing the product if found
     */
    Optional<Product> findBySkuIgnoreCase(String sku);

    /**
     * Find all products in a specific category.
     *
     * @param category the category name
     * @return list of products in that category
     */
    List<Product> findByCategory(String category);

    /**
     * Find all products in a category (case-insensitive).
     *
     * @param category the category name
     * @return list of products in that category
     */
    List<Product> findByCategoryIgnoreCase(String category);

    /**
     * Find all products ordered by price in descending order.
     *
     * @return list of products sorted by price descending
     */
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    List<Product> findAllOrderByPriceDesc();

    /**
     * Find all products ordered by price in ascending order.
     *
     * @return list of products sorted by price ascending
     */
    @Query("SELECT p FROM Product p ORDER BY p.price ASC")
    List<Product> findAllOrderByPriceAsc();

    /**
     * Find products with low stock (qty <= threshold).
     *
     * @param threshold the stock quantity threshold
     * @return list of products with stock <= threshold
     */
    @Query("SELECT p FROM Product p WHERE p.stockQty <= :threshold ORDER BY p.stockQty ASC")
    List<Product> findLowStockProducts(int threshold);

    /**
     * Search products by name containing text (case-insensitive).
     *
     * @param name partial product name
     * @return list of matching products
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Get all distinct categories.
     *
     * @return list of distinct category names
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL")
    List<String> findAllCategories();
}


