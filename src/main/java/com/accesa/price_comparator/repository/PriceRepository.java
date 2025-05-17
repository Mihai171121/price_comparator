package com.accesa.price_comparator.repository;

import com.accesa.price_comparator.model.Price;
import com.accesa.price_comparator.model.Product;
import com.accesa.price_comparator.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PriceRepository extends JpaRepository<Price, Long> {
    // Search prices for a specific product
    List<Price> findByProduct(Product product);
    List<Price> findByProductId(String productId);

    // Prices for a product at a specific store, ordered by date
    List<Price> findByProductAndStoreOrderByDate(Product product, Store store);

    // Find the lowest price for a product (custom method with ascending OrderBy and limit)
    Price findTopByProductOrderByPriceAsc();

    // Find all prices from a specific date (if needed)
    List<Price> findByDate(LocalDate date);
}
