package com.accesa.price_comparator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "discount")
@Data
@NoArgsConstructor
public class Discount {

    // Cheie primară autogenerată
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Identificarea produsului şi detalii redundante
    private String productId;          // codul produsului
    private String productName;        // numele produsului (snapshot la momentul reducerii)
    private String brand;              // marca produsului
    private double packageQuantity;    // ex.: 1, 0.5, 500
    private String packageUnit;        // ex.: "kg", "l", "g", "pcs"
    private String productCategory;    // categoria produsului

    // Datele reducerii
    private LocalDate fromDate;        // data început
    private LocalDate toDate;          // data sfârşit
    private int percentageOfDiscount;            // procent reducere (ex.: 10 pentru 10%)

    // Magazinul în care este valabilă reducerea-
    @ManyToOne(optional = false)
    @JoinColumn(name = "store_name", referencedColumnName = "name", nullable = false)
    private Store store;               // magazinul (FK către store.name)

    // Data preţului de referinţă (snapshot la care s‑a calculat reducerea)
    private LocalDate priceDate;
}
