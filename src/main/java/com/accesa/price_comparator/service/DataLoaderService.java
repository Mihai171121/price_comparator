package com.accesa.price_comparator.service;

import com.accesa.price_comparator.model.*;
import com.accesa.price_comparator.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;

@Component
public class DataLoaderService implements CommandLineRunner {
    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private StoreRepository storeRepo;
    @Autowired
    private PriceRepository priceRepo;
    @Autowired
    private DiscountRepository discountRepo;

    // List of import file names (we manually list them here)
    private String[] dataFiles = new String[] {
            "lidl_2025-05-01.csv",
            "profi_2025-05-01.csv",
            "kaufland_2025-05-01.csv",
            "lidl_2025-05-08.csv",
            "profi_2025-05-08.csv",
            "kaufland_2025-05-08.csv",
            "lidl_discounts_2025-05-01.csv",
            "profi_discounts_2025-05-01.csv",
            "kaufland_discounts_2025-05-01.csv",
            "lidl_discounts_2025-05-08.csv",
            "profi_discounts_2025-05-08.csv",
            "kaufland_discounts_2025-05-08.csv"
    };

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Starting data import from CSV files... ===");
        for (String fileName : dataFiles) {
            importCsvFile(fileName);
        }
        System.out.println("=== CSV file data import completed. ===");
    }

    private void importCsvFile(String fileName) throws Exception {
        // Open the file from the resources folder
        ClassPathResource resource = new ClassPathResource(fileName);
        if (!resource.exists()) {
            System.err.println("File " + fileName + " was not found in resources, skipping.");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String headerLine = reader.readLine(); // read the first line (header) and ignore it
            if (headerLine == null) {
                System.err.println("File " + fileName + " is empty or corrupted.");
                return;
            }

            // Determine whether it is a discount or a price file
            boolean isDiscountFile = fileName.contains("discount");
            // Extract store name and date from the file name
            String storeName;
            LocalDate date;
            if (isDiscountFile) {
                // ex: "lidl_discounts_2025-05-01.csv" -> storeName = "lidl"
                storeName = fileName.substring(0, fileName.indexOf("_discount"));
                // date comes after "_discounts_"
                String dateStr = fileName.substring(fileName.lastIndexOf("_") + 1, fileName.length() - 4);
                date = LocalDate.parse(dateStr);
            } else {
                // ex: "lidl_2025-05-01.csv" -> storeName = "lidl"
                storeName = fileName.substring(0, fileName.indexOf("_"));
                String dateStr = fileName.substring(fileName.indexOf("_") + 1, fileName.length() - 4);
                date = LocalDate.parse(dateStr);
            }

            // Make sure the store exists in the DB (or create it)
            Store store = storeRepo.findById(storeName).orElse(null);
            if (store == null) {
                store = new Store();
                store.setName(storeName);
                storeRepo.save(store);
            }

            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // skip empty lines, if any
                String[] fields = line.split(";");
                try {
                    if (!isDiscountFile) {
                        // Price file: fields correspond to:
                        // 0: product_id, 1: name, 2: category, 3: brand, 4: package_quantity, 5: package_unit, 6: price, 7: currency
                        String productId = fields[0];
                        String name = fields[1];
                        String category = fields[2];
                        String brand = fields[3];
                        double packageQty = Double.parseDouble(fields[4]);
                        String packageUnit = fields[5];
                        double priceValue = Double.parseDouble(fields[6]);
                        String currency = fields[7];

                        // Product
                        Product product = productRepo.findById(productId).orElse(null);
                        if (product == null) {
                            product = new Product();
                            product.setId(productId);
                            product.setName(name);
                            product.setCategory(category);
                            product.setBrand(brand);
                            product.setPackageQuantity(packageQty);
                            product.setPackageUnit(packageUnit);
                            productRepo.save(product);
                        } else {
                            // If the product already exists, we can optionally update attributes
                            // Normally, name/category shouldn't differ for the same productId
                            // We can still check for consistency:
                            if (!product.getName().equals(name) || !product.getCategory().equals(category)) {
                                System.out.println("Info inconsistency: Product " + productId + " has different names or categories in file " + fileName);
                            }
                            // We do not update brand even if it differs – we keep the first one
                        }
                        // Price
                        Price price = new Price();
                        price.setProduct(product);
                        price.setStore(store);
                        price.setDate(date);
                        price.setPrice(priceValue);
                        price.setCurrency(currency);
                        priceRepo.save(price);
                        count++;
                    } else {
                        // Discount file: fields:
                        // 0: product_id, 1: name, 2: brand, 3: package_quantity, 4: package_unit, 5: category, 6: from_date, 7: to_date, 8: percentage_of_discount
                        String productId = fields[0];
                        // [1]=name, [2]=brand,... [5]=category (not necessarily needed if product already exists)
                        LocalDate fromDate = LocalDate.parse(fields[6]);
                        LocalDate toDate = LocalDate.parse(fields[7]);
                        int percentage = Integer.parseInt(fields[8]);

                        Product product = productRepo.findById(productId).orElse(null);
                        if (product == null) {
                            // If the product wasn't in the price list, we create a minimal version
                            product = new Product();
                            product.setId(productId);
                            product.setName(fields[1]);
                            product.setCategory(fields[5]);
                            product.setBrand(fields[2]);
                            product.setPackageQuantity(Double.parseDouble(fields[3]));
                            product.setPackageUnit(fields[4]);
                            productRepo.save(product);
                        }
                        Discount discount = new Discount();
                        discount.setProduct(product);
                        discount.setStore(store);
                        discount.setFromDate(fromDate);
                        discount.setToDate(toDate);
                        discount.setPercentage(percentage);
                        discountRepo.save(discount);
                        count++;
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing line: \"" + line + "\" from " + fileName + " -> " + e.getMessage());
                }
            } // end while lines
            if (!isDiscountFile) {
                System.out.println("Imported " + count + " prices for store " + storeName + " (date " + date + ")");
            } else {
                System.out.println("Imported " + count + " discounts for store " + storeName + " (promotion start date " + date + ")");
            }
        }
    }
}
