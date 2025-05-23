package com.accesa.price_comparator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A user-defined alert that fires when a product price
 * drops to (or below) the desired targetPrice.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder //TODO: builder and AllArgsConstructor might be already added by @ Data
public class PriceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // we store the FK as simple Long; no need for @ManyToOne here
    private Long productId;

    private double targetPrice;

    /** mark true once the alert is satisfied */
    private boolean triggered;
}
