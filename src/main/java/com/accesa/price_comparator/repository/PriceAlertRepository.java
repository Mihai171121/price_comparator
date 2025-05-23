package com.accesa.price_comparator.repository;

import com.accesa.price_comparator.model.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    List<PriceAlert> findByTriggeredFalse();
    List<PriceAlert> findByProductId(Long productId);
}
