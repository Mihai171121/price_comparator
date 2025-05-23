package com.accesa.price_comparator.repository;

import com.accesa.price_comparator.model.Product;
import com.accesa.price_comparator.model.Product.ProductKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, ProductKey> {
    // Căutare produse după nume (poate returna multiple rezultate dacă același produs există în mai multe magazine)
    List<Product> findByProductName(String productName);

    // Găsește toate înregistrările produsului după cod (id-ul produsului)
    List<Product> findByKeyId(String id);

    // Găsește înregistrările produsului după cod și magazin (ignorând data, returnează istoricul prețurilor în acel magazin)
    List<Product> findByKeyIdAndKeyStoreName(String id, String storeName);

    /**
     * Returnează cea mai nouă înregistrare (după priceDate desc) pentru codul de produs dat.
     *  - key.id         → cola codul produsului (segmentul „id” din cheia compusă)
     *  - key.priceDate  → segmentul „priceDate” din cheia compusă
     */
    Product findTopByKeyIdOrderByKeyPriceDateDesc(String id);
}
