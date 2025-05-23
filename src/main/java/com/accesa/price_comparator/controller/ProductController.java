package com.accesa.price_comparator.controller;

import com.accesa.price_comparator.model.Product;
import com.accesa.price_comparator.model.Store;
import com.accesa.price_comparator.service.PriceAnalysisService;
import com.accesa.price_comparator.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final PriceAnalysisService analysisService;
    private final ProductService  productService;

    public ProductController(PriceAnalysisService analysisService, ProductService productService) {
        this.analysisService = analysisService;
        this.productService = productService;
    }

    // 1. List of products (optionally filtered by category or name)
    @GetMapping("/product")
    public List<Product> getProducts(@RequestParam(required = false) String category,
                                     @RequestParam(required = false) String name) {
        if (category != null && !category.isEmpty()) {
            return analysisService.getProductsByCategory(category);
        }
        if (name != null && !name.isEmpty()) {
            return analysisService.searchProductsByName(name);
        }
        // If no parameters are specified, return all products
        return analysisService.getProductsByCategory("");  // (empty category -> returns the full list)
    }

    // 2. Product details (basic information + price offers per store)
    @GetMapping("/products/{id}")
    public List<Product> getProductDetails(@PathVariable String id) {
      return   productService.getProductById(id);
    }

    // 3. GetLatestPrice for products in store
    @GetMapping("/compare/{productId}")
    public List<Product> comparePrices(@PathVariable String productId) {
        return analysisService.comparePricesForProduct(productId);
    }

    // 4. Price history of a product (optionally filtered by a specific store)
    @GetMapping("/history/{productId}")
    public List<Product> priceHistory(@PathVariable String productId,
                                      @RequestParam(required = false) String store) {
        return analysisService.getPriceHistory(productId, store);
    }

    //TODO: move to alert controller
    // 5. Check whether a product has reached a price threshold (price alert)
    @GetMapping("/alerts/check")
    public Map<String, Object> checkPriceAlert(@RequestParam String productId,
                                               @RequestParam double targetPrice) {
        boolean belowTarget = analysisService.checkPriceBelowTarget(productId, targetPrice);
        return Map.of(
                "productId", productId,
                "targetPrice", targetPrice,
                "belowTarget", belowTarget
        );
    }


    // 6. Basket optimisation (distribute products across stores for minimum total cost)
    @GetMapping("/basket/optimize")
    public Map<String, Object> optimizeBasket(@RequestParam List<String> products) {
        Map<Store, List<Product>> allocation = analysisService.optimizeBasket(products);
        // Convert the Store key to the store name to simplify the JSON structure of the response
        Map<String, List<Product>> result = new HashMap<>();
        allocation.forEach((store, productList) ->
                result.put(store.getName(), productList)
        );
        // Calculate the total cost of the optimised basket
        double totalCost = result.values().stream()
                .flatMap(List::stream)
                .mapToDouble(Product::getPrice)
                .sum();
        return Map.of(
                "allocation", result,
                "totalCost", totalCost
        );
    }

    // 7. Product substitute/recommendation endpoint:Returns a cheaper alternative product (same category) based on unit price, if any.
    @GetMapping("/{id}/alternative")
    public Product getAlternative(@PathVariable String id) {
        return analysisService.getBestValueAlternative(id);
    }
}