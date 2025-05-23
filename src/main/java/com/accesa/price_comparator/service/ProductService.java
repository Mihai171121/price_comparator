package com.accesa.price_comparator.service;

import com.accesa.price_comparator.model.Product;
import com.accesa.price_comparator.repository.ProductRepository;
import com.accesa.price_comparator.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer pentru operatii legate de entitatea {@link Product}.
 *  - ofera metode de listare / cautare
 *  - importa datele dintr-un CSV (format: product_id;product_name;product_category;brand;package_quantity;package_unit;price;currency;storeName;priceDate)
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    /**
     * Returneaza lista tuturor produselor din baza.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }


    public List<Product> getProductById(String id) {
        return productRepository.findByKeyId(id);
    }
}
