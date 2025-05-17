package com.accesa.price_comparator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "prices")
@Data
@NoArgsConstructor
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;          // auto-generated primary key (internal ID for each price record)

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")   // FK column to Product (column name in the "prices" table)
    private Product product;           // reference to the product this price belongs to

    @ManyToOne(optional = false)
    @JoinColumn(name = "store_name")   // FK column to Store
    private Store store;               // reference to the store where the price is valid

    private LocalDate date;    // date when the price is valid (e.g., 2025-05-01)
    private double price;      // price value
    private String currency;   // currency (e.g., "RON")
}
