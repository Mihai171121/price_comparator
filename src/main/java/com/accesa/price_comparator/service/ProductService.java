package com.accesa.price_comparator.service;

import com.accesa.price_comparator.model.Product;
import com.accesa.price_comparator.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;

@Service  // Marchează clasa ca un component de tip Service (logica de business)
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    // Injectăm automat repository-ul de Product (Dependency Injection).
    // Astfel putem folosi productRepository pentru operații DB.

    public List<Product> getAllProducts() {
        // Obține toate produsele din baza de date
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        // Caută un produs după ID și returnează-l dacă există, altfel null
        return productRepository.findById(id.toString()).orElse(null);
    }

    public void importProductsFromCsv(String filePath) {
        // Citește produsele dintr-un fișier CSV de la locația specificată și le salvează în DB
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource(filePath).getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                // Dacă prima linie este header, o sărim
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                // Împărțim linia CSV în câmpuri, presupunând că separatorul este virgulă
                String[] fields = line.split(",");
                if (fields.length < 2) {
                    continue; // linie invalidă sau incompletă
                }
                String name = fields[0].trim();
                double price = 0;
                try {
                    price = Double.parseDouble(fields[1].trim());
                } catch (NumberFormatException e) {
                    // Dacă prețul nu e număr valid, continuăm la următoarea linie
                    continue;
                }
                // Creăm un obiect Product și setăm valorile citite
                Product product = new Product();
                product.setName(name);
                product.setPrice(price);
                // Salvăm produsul în baza de date prin repository
                productRepository.save(product);
            }
            System.out.println("Import din CSV finalizat cu succes.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void loadInitialData() {
        // La pornirea aplicației, importăm datele din CSV-ul "products.csv" aflat în resources.
        importProductsFromCsv("products.csv");
    }
}
