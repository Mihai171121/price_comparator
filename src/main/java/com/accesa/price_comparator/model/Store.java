package com.accesa.price_comparator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
public class Store {

    @Id
    private String name;   // We use the store name as the primary key (e.g., "Lidl", "Kaufland")

    // We could also add other fields (e.g., address, if we had one). In our data, it's not needed.
}
