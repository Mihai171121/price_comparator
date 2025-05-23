package com.accesa.price_comparator.service;

import com.accesa.price_comparator.model.Discount;
import com.accesa.price_comparator.model.Product;
import com.accesa.price_comparator.model.Store;
import com.accesa.price_comparator.repository.DiscountRepository;
import com.accesa.price_comparator.repository.ProductRepository;
import com.accesa.price_comparator.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceAnalysisService {

    private final ProductRepository productRepo;
    private final StoreRepository storeRepo;
    private final DiscountRepository discountRepo;
    //TODO: injection via constructor
    public PriceAnalysisService(ProductRepository productRepo,
                                StoreRepository storeRepo,
                                DiscountRepository discountRepo) {
        this.productRepo = productRepo;
        this.storeRepo = storeRepo;
        this.discountRepo = discountRepo;
    }

    /**
     * Compares the current prices of a product (identified by its code) across all stores
     * and returns a list of Product records (one per store), sorted in ascending order by price.
     */
    public List<Product> comparePricesForProduct(String productId) {
        // Check if there's at least one product with this ID
        List<Product> allEntries = productRepo.findByKeyId(productId);
        if (allEntries.isEmpty()) {
            return Collections.emptyList();
        }
        List<Product> latestPrices = new ArrayList<>();
        // For each store, get the most recent known price of the product
        List<Store> stores = storeRepo.findAll();
        for (Store store : stores) {
            List<Product> pricesForProductInStore = productRepo.findByKeyIdAndKeyStoreName(productId, store.getName());
            if (!pricesForProductInStore.isEmpty()) {
                // Sort by date and get the most recent entry (last one)
                pricesForProductInStore.sort(Comparator.comparing(Product::getPriceDate));
                Product lastPrice = pricesForProductInStore.get(pricesForProductInStore.size() - 1);
                latestPrices.add(lastPrice);
            }
        }

        List<Store> allStoresNotNull = stores.stream()
                .filter(store -> store.getName() != null)
                .toList();

        // Sort the price list from lowest to highest
        latestPrices.sort(Comparator.comparingDouble(Product::getPrice));
        return latestPrices;
    }

    /**
     * Returns products from a given category, sorted alphabetically by name.
     * (May include multiple entries per product if it exists in multiple stores.)
     */
    public List<Product> getProductsByCategory(String category) {
        return productRepo.findAll().stream()
                .filter(p -> p.getProductCategory().equalsIgnoreCase(category))
                .sorted(Comparator.comparing(Product::getProductName))
                .collect(Collectors.toList());
    }

    /** Searches for products whose name contains the given string (case insensitive). */
    public List<Product> searchProductsByName(String namePart) {
        String lower = namePart.toLowerCase();
        return productRepo.findAll().stream()
                .filter(p -> p.getProductName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    /** Checks if the current minimum price of a certain product is below a specified threshold. */
    public boolean checkPriceBelowTarget(String productId, double targetPrice) {
        List<Product> comp = comparePricesForProduct(productId);
        if (comp.isEmpty()) return false;
        Product cheapest = comp.get(0);
        return cheapest.getPrice() <= targetPrice;
    }

    /**
     * Returns the price history for a given product (optionally filtered by store).
     * The list is sorted in ascending order by date.
     */
    public List<Product> getPriceHistory(String productId, String storeName) {
        List<Product> entries;
        // Check if the product exists
        List<Product> allEntries = productRepo.findByKeyId(productId);
        if (allEntries.isEmpty()) return Collections.emptyList();
        if (storeName == null || storeName.isEmpty()) {
            // All entries (from all stores) for the given product
            entries = new ArrayList<>(allEntries);
        } else {
            // Filter entries for the specified store
            Optional<Store> storeOpt = storeRepo.findById(storeName);
            if (storeOpt.isEmpty()) return Collections.emptyList();
            entries = productRepo.findByKeyIdAndKeyStoreName(productId, storeName);
        }
        // Sort by date (ascending)
        entries.sort(Comparator.comparing(Product::getPriceDate));
        return entries;
    }

    /**
     * Finds the most cost-effective alternative (a similar product from the same category)
     * based on unit price. Returns the alternative product if one has a lower unit price, otherwise returns null.
     */
    public Product getBestValueAlternative(String productId) {
        List<Product> allEntries = productRepo.findByKeyId(productId);
        if (allEntries.isEmpty()) return null;
        Product reference = allEntries.get(0);  // use the first entry just to get reference details
        String category = reference.getProductCategory();
        String productName = reference.getProductName();
        // Collect all candidates in the same category (including the original product)
        List<Product> candidates = productRepo.findAll().stream()
                .filter(p -> p.getProductCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) return null;
        // Calculate the unit price of the original product (lowest current price divided by quantity)
        double originalUnitPrice = calcMinUnitPrice(productId, reference);
        Product bestProduct = reference;
        double bestUnitPrice = originalUnitPrice;
        // Group candidates by product code to avoid duplicate evaluations
        Map<String, List<Product>> candidatesByCode = candidates.stream()
                .collect(Collectors.groupingBy(p -> p.getId()));
        for (String code : candidatesByCode.keySet()) {
            if (code.equalsIgnoreCase(productId)) continue; // skip the original product
            Product sample = candidatesByCode.get(code).get(0);  // get one instance for details
            double unitPrice = calcMinUnitPrice(code, sample);
            if (unitPrice > 0 && unitPrice < bestUnitPrice) {
                bestUnitPrice = unitPrice;
                bestProduct = sample;
            }
        }
        return (bestProduct == reference) ? null : bestProduct;
    }

    // Helper method: calculates the lowest unit price for the given product (minimum price / quantity)
    private double calcMinUnitPrice(String productCode, Product productInfo) {
        List<Product> comp = comparePricesForProduct(productCode);
        if (comp.isEmpty()) return -1;
        Product cheapest = comp.get(0);
        double qty = productInfo.getPackageQuantity();
        String unit = productInfo.getPackageUnit();
        // Normalize quantity to base units (e.g., grams -> kg, ml -> l)
        if (unit.equalsIgnoreCase("g")) {
            qty = qty / 1000.0;
        } else if (unit.equalsIgnoreCase("ml")) {
            qty = qty / 1000.0;
        }
        if (qty <= 0) return -1;
        return cheapest.getPrice() / qty;
    }

    /**
     * Returns an optimized allocation of a list of products (by code) across stores,
     * such that the total cost is minimized.
     * The result is a map: Store -> List of products bought from that store.
     */
    public Map<Store, List<Product>> optimizeBasket(List<String> productIds) {
        Map<Store, List<Product>> allocation = new HashMap<>();
        for (String pid : productIds) {
            List<Product> comp = comparePricesForProduct(pid);
            if (comp.isEmpty()) continue;
            Product cheapest = comp.get(0);
            Store store = cheapest.getStore();
            allocation.computeIfAbsent(store, k -> new ArrayList<>());
            allocation.get(store).add(cheapest);
        }
        return allocation;
    }

    /**
     * Returnează cele mai mari N reduceri active la data curentă,
     * ordonate descrescător după procentul de reducere.
     */
    public List<Discount> getBestDiscounts(int top) {
        LocalDate today = LocalDate.now();

        // toate reducerile valide astăzi
        List<Discount> active = discountRepo
                .findByFromDateLessThanEqualAndToDateGreaterThanEqual(today, today);

        // ordonăm descrescător și limităm la "top"
        return active.stream()
                .sorted(Comparator.comparingInt(Discount::getPercentageOfDiscount).reversed())
                .limit(top)
                .collect(Collectors.toList());
    }

    /**
     * Returnează reducerile adăugate în ultimele 24 h
     * (presupunem că "fromDate" ≈ data când au fost încărcate).
     */
    public List<Discount> getNewDiscounts() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // toate reducerile care AU ÎNCEPUT după ieri (=> în ultimele 24 h)
        return discountRepo.findByFromDateAfter(yesterday);
    }

}
