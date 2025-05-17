package com.accesa.price_comparator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "discounts")
@Data
@NoArgsConstructor
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "store_name")
    private Store store;

    private LocalDate fromDate;
    private LocalDate toDate;
    private int percentage;   // discount percentage (e.g., 10 for 10%)
}
