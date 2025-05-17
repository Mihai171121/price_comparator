package com.accesa.price_comparator.service;

import com.accesa.price_comparator.model.*;
import com.accesa.price_comparator.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceAnalysisService {

    private final ProductRepository productRepo;
    private final PriceRepository priceRepo;
    private final StoreRepository storeRepo;
    private final DiscountRepository discountRepo;

    public PriceAnalysisService(ProductRepository productRepo, PriceRepository priceRepo,
                                StoreRepository storeRepo, DiscountRepository discountRepo){
        this.productRepo = productRepo;
        this.priceRepo = priceRepo;
        this.storeRepo = storeRepo;
        this.discountRepo = discountRepo;
    }

    /** Compares the current prices of a product across all stores and returns a list sorted by ascending price. */
    public List<Price> comparePricesForProduct(String productId){
        Optional<Product> optProduct = productRepo.findById(productId);
        if(optProduct.isEmpty()){
            return Collections.emptyList();
        }
        Product product = optProduct.get();
        List<Price> latestPrices = new ArrayList<>();
        // For each store, get the latest known price of the product
        for (Store store : storeRepo.findAll()){
            List<Price> prices = priceRepo.findByProductAndStoreOrderByDate(product, store);
            if (!prices.isEmpty()){
                Price lastPrice = prices.get(prices.size() - 1); // last in the list (ordered by date ascending)
                latestPrices.add(lastPrice);
            }
        }
        // Sort prices from lowest to highest
        latestPrices.sort(Comparator.comparingDouble(Price::getPrice));
        return latestPrices;
    }

    /** Returns products from a given category, sorted alphabetically by name. */
    public List<Product> getPructsByCategory(String category){
        // Using Java Streams: filter products by category and sort
        return productRepo.findAll().stream().filter(p -> p.getCategory().equalsIgnoreCase(category))
                .sorted(Comparator.comparing(Product::getName)).collect(Collectors.toList());
    }

    /** Searches for products by partial name (case-insensitive). */
    public List<Product> searchProductByName(String namePart){
        String lower = namePart.toLowerCase();
        return productRepo.findAll().stream().filter((p -> p.getName().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }

    /** Returns the price history for a product */
    public List<Price> getPriceHistory(String productId, String storeName){
        Optional<Product> opt = productRepo.findById(productId);
        if(opt.isEmpty()) return Collections.emptyList();
        Product product = opt.get();
        List<Price> prices;
        if (storeName == null || storeName.isEmpty()) {
            // All prices of the product across all stores
            prices = priceRepo.findByProduct(product);
        } else {
            Optional<Store> storeOpt = storeRepo.findById(storeName);
            if(storeOpt.isEmpty()) return Collections.emptyList();
            prices = priceRepo.findByProductAndStoreOrderByDate(product, storeOpt.get());
        }
        // Sort by date
        prices.sort(Comparator.comparing(Price::getDate));
        return prices;
    }

    /** Determines which similar product (in the same category) offers the best value in terms of price per unit. */
    public Product getBestValueAlternative(String productId) {
        Optional<Product> opt = productRepo.findById(productId);
        if (opt.isEmpty()) return null;
        Product original = opt.get();
        String category = original.getCategory();
        String producName = original.getName();
        // Collect products from the same category
        List<Product> candidates = productRepo.findAll().stream().filter(p -> p.getCategory()
                .equalsIgnoreCase(category)).collect(Collectors.toList());
        if (candidates.isEmpty()) return null;
        // Calculate unit price for the original product
        double originalUnitPrice = calcMinUnitPrice(original);
        Product bestProduct = original;
        double bestUnitPrice = originalUnitPrice;
        for (Product p : candidates) {
            double unitPrice = calcMinUnitPrice(p);
            if (unitPrice > 0 && unitPrice < bestUnitPrice - 1e-6) {
                // Compare with small tolerance
                bestUnitPrice = unitPrice;
                bestProduct = p;
            }
        }
        if (bestProduct.equals(original)) {
            return null; // original product is already the best option
        } else {
            return bestProduct;
        }
    }

    /** Helper: calculates the lowest unit price for a product (minimum current price / quantity) */
    private double calcMinUnitPrice(Product product) {
        // Find the lowest current price for the product (across all stores)
        List<Price> comp = comparePricesForProduct(product.getId());
        if (comp.isEmpty()) return -1;
        Price cheapest = comp.get(0);
        // Calculate unit price: price / quantity, convert to base unit if necessary (e.g., g to kg)
        double qty = product.getPackageQuantity();
        String unit = product.getPackageUnit();
        if (unit.equalsIgnoreCase("g")) {
            // If it's in grams, convert to kg: 1000g = 1kg
            qty = qty / 1000.0;
        }
        if (qty == 0) return -1;
        return cheapest.getPrice() / qty;
    }

    /** Finds the currently active discounts (today), ordered by percentage descending and returns top N. */
    public List<Discount> getBestDiscounts(int topN) {
        LocalDate today = LocalDate.now();
        List<Discount> activeDiscounts = discountRepo.findByFromDateLessThanEqualAndToDateGreaterThanEqual(today, today);
        if (activeDiscounts.isEmpty()) return Collections.emptyList();
        activeDiscounts.sort((d1, d2) -> Integer.compare(d2.getPercentage(), d1.getPercentage()));
        if (activeDiscounts.size() > topN) {
            return activeDiscounts.subList(0, topN);
        } else {
            return activeDiscounts;
        }
    }

    /** Finds discounts that started recently (in the last 24 hours). */
    public List<Discount> getNewDIscounts() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        // Discounts with fromDate after yesterday → those that start yesterday or today
        List<Discount> newDiscounts = discountRepo.findByFromDateAfter(yesterday);
        // Filter to include only those with fromDate <= today
        newDiscounts = newDiscounts.stream().filter(d -> !d.getFromDate().isAfter(today))
                .collect(Collectors.toList());
        return newDiscounts;
    }

    /** Splits a given product list across stores for the lowest total cost.
     * Returns a mapping: store → list of prices (products bought from that store) */
    public Map<Store, List<Price>> optimizeBasket(List<String> productIds) {
        Map<Store, List<Price>> allocation = new HashMap<>();
        for (String pid : productIds) {
            List<Price> comp = comparePricesForProduct(pid);
            if (comp.isEmpty()) continue;
            Price cheapest = comp.get(0); // store with the lowest price
            Store store = cheapest.getStore();
            allocation.computeIfAbsent(store, k -> new ArrayList<>());
            allocation.get(store).add(cheapest);
        }
        return allocation;
    }
}
