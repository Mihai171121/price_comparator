package com.accesa.price_comparator.repository;

import com.accesa.price_comparator.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
    // Custom methods (derived queries):
    Product findByName(String name);
    // we can add others, for example:
    // List<Product> findByCategory(String category);
}
