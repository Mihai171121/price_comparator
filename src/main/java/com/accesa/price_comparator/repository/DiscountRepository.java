package com.accesa.price_comparator.repository;

import com.accesa.price_comparator.model.Discount;
import com.accesa.price_comparator.model.Product;
import com.accesa.price_comparator.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
    // Finds active discounts on a specific date (fromDate <= date <= toDate)
    List<Discount> findByFromDateLessThanEqualAndToDateGreaterThanEqual(LocalDate date1, LocalDate date2);

    // Discounts for a specific product
    List<Discount> findByProduct(Product product);

    // Discounts that started after a specific date (for "new discounts")
    List<Discount> findByFromDateAfter(LocalDate date);

    // The highest current discount (among those active at the given moment)
    Discount findTopByFromDateLessThanEqualAndToDateGreaterThanEqualOrderByPercentageDesc(LocalDate today1, LocalDate today2);
}
