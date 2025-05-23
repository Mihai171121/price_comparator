package com.accesa.price_comparator.repository;

import com.accesa.price_comparator.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    // Găsește toate reducerile active la o anumită dată (fromDate <= date <= toDate)
    List<Discount> findByFromDateLessThanEqualAndToDateGreaterThanEqual(LocalDate date1, LocalDate date2);

    // Găsește toate reducerile care au început după o anumită dată (pentru a identifica reducerile noi)
    List<Discount> findByFromDateAfter(LocalDate date);

    // Găsește cea mai mare reducere activă la o anumită dată (ordonează descendent după procentaj și limitează la prima)
    Discount findTopByFromDateLessThanEqualAndToDateGreaterThanEqualOrderByPercentageOfDiscountDesc(LocalDate date1, LocalDate date2);
}
