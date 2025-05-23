package com.accesa.price_comparator.controller;

import com.accesa.price_comparator.model.PriceAlert;
import com.accesa.price_comparator.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dedicated controller for Custom Price Alert feature.
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class PriceAlertController {

    private final PriceAlertService alertService;

    /** Create a new alert: POST /api/alerts?productId=1&targetPrice=9.99 */
    @PostMapping
    public PriceAlert createAlert(@RequestParam Long productId,
                                  @RequestParam double targetPrice) {
        return alertService.createAlert(productId, targetPrice);
    }

    /** List all non-triggered alerts */
    @GetMapping("/active")
    public List<PriceAlert> activeAlerts() {
        return alertService.getActiveAlerts();
    }

    /** Manually mark an alert as triggered */
    @PutMapping("/{id}/trigger")
    public PriceAlert trigger(@PathVariable Long id) {
        return alertService.getActiveAlerts()
                .stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .map(alertService::markTriggered)
                .orElse(null);
    }
}
