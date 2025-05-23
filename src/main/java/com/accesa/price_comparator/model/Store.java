package com.accesa.price_comparator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
//@Table(name = "store")
@Data
@NoArgsConstructor
public class Store {

    @Id
    private String name;   // We use the store name as the primary key (e.g., "Lidl", "Kaufland")
}
