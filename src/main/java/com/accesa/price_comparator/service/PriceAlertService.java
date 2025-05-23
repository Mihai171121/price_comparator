package com.accesa.price_comparator.service;

import com.accesa.price_comparator.model.PriceAlert;
import com.accesa.price_comparator.repository.PriceAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Encapsulates all alert-related business logic.
 */
@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private final PriceAlertRepository alertRepo;

    public PriceAlert createAlert(Long productId, double targetPrice) {
        PriceAlert alert = PriceAlert.builder()
                .productId(productId)
                .targetPrice(targetPrice)
                .triggered(false)
                .build();
        return alertRepo.save(alert);
    }

    public List<PriceAlert> getActiveAlerts() {
        return alertRepo.findByTriggeredFalse();
    }

    public PriceAlert markTriggered(PriceAlert alert) {
        alert.setTriggered(true);
        return alertRepo.save(alert);
    }
}
