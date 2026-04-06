package com.mcp.mcp_server.tools;

import com.mcp.mcp_server.entity.Product;
import com.mcp.mcp_server.repository.ProductRepository;
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
 * MCP Tools for querying the products table.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTools {

    private final ProductRepository productRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 1 – list all products
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Retrieves all products from the database, sorted by price descending.
            Returns id, name, category, price, stock quantity and SKU for each product.
            """)
    public List<Map<String, Object>> getAllProducts() {
        log.debug("MCP tool called: getAllProducts");
        return productRepository.findAllOrderByPriceDesc()
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 2 – get product by id
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = "Retrieves a single product by its numeric ID.")
    public Map<String, Object> getProductById(
            @ToolParam(description = "The numeric ID of the product") Long id) {

        log.debug("MCP tool called: getProductById({})", id);
        return productRepository.findById(id)
                .map(this::toMap)
                .orElse(Map.of("error", "Product not found with id: " + id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 3 – get product by SKU
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Retrieves a product by its SKU (Stock Keeping Unit) code.
            The SKU lookup is case-insensitive, e.g. 'elec-lp15' matches 'ELEC-LP15'.
            """)
    public Map<String, Object> getProductBySku(
            @ToolParam(description = "The SKU code of the product, e.g. ELEC-LP15") String sku) {

        log.debug("MCP tool called: getProductBySku({})", sku);
        return productRepository.findBySkuIgnoreCase(sku)
                .map(this::toMap)
                .orElse(Map.of("error", "Product not found with SKU: " + sku));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 4 – search products by name
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = "Searches products by a partial name match (case-insensitive).")
    public List<Map<String, Object>> searchProductsByName(
            @ToolParam(description = "Partial product name to search, e.g. 'laptop'") String name) {

        log.debug("MCP tool called: searchProductsByName({})", name);
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 5 – get products by category
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = "Retrieves all products in a given category (case-insensitive).")
    public List<Map<String, Object>> getProductsByCategory(
            @ToolParam(description = "Category name, e.g. Electronics, Furniture, Stationery") String category) {

        log.debug("MCP tool called: getProductsByCategory({})", category);
        return productRepository.findByCategoryIgnoreCase(category)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 6 – get low-stock products
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = """
            Returns all products whose stock quantity is below the given threshold.
            Useful for identifying items that need restocking. Results are sorted
            from lowest to highest stock.
            """)
    public List<Map<String, Object>> getLowStockProducts(
            @ToolParam(description = "Stock threshold; products with qty below this are returned. Default 20.") int threshold) {

        log.debug("MCP tool called: getLowStockProducts(threshold={})", threshold);
        return productRepository.findLowStockProducts(threshold)
                .stream()
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tool 7 – list all categories
    // ─────────────────────────────────────────────────────────────────────────
    @Tool(description = "Returns the distinct list of all product categories.")
    public List<String> getAllProductCategories() {
        log.debug("MCP tool called: getAllProductCategories");
        return productRepository.findAllCategories();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────
    private Map<String, Object> toMap(Product p) {
        return Map.of(
                "id",        p.getId(),
                "name",      p.getName(),
                "category",  p.getCategory() != null ? p.getCategory() : "N/A",
                "price",     p.getPrice(),
                "stockQty",  p.getStockQty() != null ? p.getStockQty() : 0,
                "sku",       p.getSku() != null ? p.getSku() : "N/A",
                "createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : "N/A"
        );
    }
}
