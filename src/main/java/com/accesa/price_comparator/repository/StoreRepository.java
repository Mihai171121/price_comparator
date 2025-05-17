package com.accesa.price_comparator.repository;

import com.accesa.price_comparator.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, String> {
    // We can add custom methods if needed, for example:
    // boolean existsByName(String name);
}
