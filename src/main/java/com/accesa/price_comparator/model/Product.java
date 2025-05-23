package com.accesa.price_comparator.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * key represents combination of
 */

@Entity
@Table(name = "product", uniqueConstraints = @UniqueConstraint(columnNames = {"id", "store_name", "price_date"}))
@Data
@NoArgsConstructor
public class Product {

    // Cheia compusă (id + store + date)
    @EmbeddedId
    private ProductKey key;

    // Relaţia către magazin (store_name din cheie este FK → Store.name)
    @MapsId("storeName")
    @ManyToOne(optional = false)
    @JoinColumn(name = "store_name", referencedColumnName = "name", nullable = false)
    private Store store;

    // Atribute descriptive (snapshot pentru fiecare înregistrare)
    private String productName;
    private String productCategory;
    private String brand;
    private double packageQuantity;
    private String packageUnit;

    // Atribute de preţ
    private double price;
    private String currency;

    // Gettere convenabile pentru cod şi dată (extras din cheie)
    public String getId() {
        return key != null ? key.getId() : null;
    }

    public LocalDate getPriceDate() {
        return key != null ? key.getPriceDate() : null;
    }

    // Definim cheia compusă ca @Embeddable
    @Embeddable
    @Data
    @NoArgsConstructor
    public static class ProductKey implements Serializable {
        private String id;              // cod produs
        private String storeName;       // magazin
        private LocalDate priceDate;    // data preţului
    }
}
