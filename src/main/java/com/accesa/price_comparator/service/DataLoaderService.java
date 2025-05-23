package com.accesa.price_comparator.service;

import com.accesa.price_comparator.model.Discount;
import com.accesa.price_comparator.model.Product;
import com.accesa.price_comparator.model.Store;
import com.accesa.price_comparator.repository.DiscountRepository;
import com.accesa.price_comparator.repository.ProductRepository;
import com.accesa.price_comparator.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class DataLoaderService implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private StoreRepository storeRepo;
    @Autowired
    private DiscountRepository discountRepo;
    @Autowired
    private PriceAnalysisService analysisService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n\n=== Console testing with data from PostgreSQL === \n");

        String testProductId = "P001";  // un cod de produs valid existent în datele de test

        System.out.println("\n--- MANUAL CONSOLE TESTING ---");

        // 1. Compararea prețurilor pentru un produs
        List<Product> priceComparison = analysisService.comparePricesForProduct(testProductId);
        System.out.println("\nCurrent prices for product " + testProductId + ":");
        for (Product pr : priceComparison) {
            System.out.println("  Store " + pr.getStore().getName() + " - " + pr.getPrice() + " " + pr.getCurrency());
        }
        if (!priceComparison.isEmpty()) {
            Product best = priceComparison.get(0);
            System.out.println("\n  -> The cheapest is at " + best.getStore().getName() + ": " + best.getPrice() + " " + best.getCurrency());
        }

        // 2. Produse dintr-o categorie specifică
        String category = "lactate";
        List<Product> lactate = analysisService.getProductsByCategory(category);
        System.out.println("\nProducts in category '" + category + "':");
        lactate.forEach(p ->
                System.out.println("  " + p.getId() + " - " + p.getProductName() + " (" + p.getBrand() + ")")
        );

        // 3. Alertă de preț
        double targetPrice = 10.0;
        boolean isCheap = analysisService.checkPriceBelowTarget(testProductId, targetPrice);
        if (isCheap) {
            System.out.println("\nALERT: Product " + testProductId + " dropped below " + targetPrice + " RON!");
        } else {
            System.out.println("\nProduct " + testProductId + " hasn't reached the target price of " + targetPrice + " RON yet.");
        }

        // 4. Istoricul prețurilor
        List<Product> history = analysisService.getPriceHistory(testProductId, null);
        System.out.println("\nPrice history for product " + testProductId + ":");
        history.forEach(pr ->
                System.out.println("  " + pr.getPriceDate() + " - " + pr.getStore().getName() + ": " + pr.getPrice() + " " + pr.getCurrency())
        );

        // 5. Cele mai mari reduceri active în prezent (top 3)
        List<Discount> topDiscounts = analysisService.getBestDiscounts(3);
        System.out.println("\nTop 3 active discounts today:");
        for (Discount d : topDiscounts) {
            // obținem numele produsului aferent reducerii (folosind productRepo și productId din discount)
            String prodName = d.getProductId();
            List<Product> prodEntries = productRepo.findByKeyIdAndKeyStoreName(d.getProductId(), d.getStore().getName());
            if (!prodEntries.isEmpty()) {
                prodName = prodEntries.get(0).getProductName();
            }
            System.out.println("  " + prodName + " at " + d.getStore().getName() + ": -"
                    + d.getPercentageOfDiscount() + "% (from " + d.getFromDate() + " to " + d.getToDate() + ")");
        }

        // 6. Reduceri noi adăugate recent (în ultimele 24h)
        List<Discount> newDiscounts = analysisService.getNewDiscounts();
        System.out.println("\nRecently added discounts:");
        for (Discount d : newDiscounts) {
            String prodName = d.getProductId();
            List<Product> prodEntries = productRepo.findByKeyIdAndKeyStoreName(d.getProductId(), d.getStore().getName());
            if (!prodEntries.isEmpty()) {
                prodName = prodEntries.get(0).getProductName();
            }
            System.out.println("  [" + d.getFromDate() + "] " + prodName + " - "
                    + d.getPercentageOfDiscount() + "% at " + d.getStore().getName());
        }

        // 7. Cea mai bună alternativă ca preț unitar pentru un produs dat
        String prodForSubstituteId = "P052";  // exemplu de cod produs (de ex.: ciocolată neagră 70%)
        Product alt = analysisService.getBestValueAlternative(prodForSubstituteId);
        if (alt != null) {
            System.out.println("\nFor product " + prodForSubstituteId + ", a cheaper alternative per unit is: "
                    + alt.getProductName() + " (" + alt.getBrand() + ")");
        } else {
            System.out.println("\nProduct " + prodForSubstituteId + " already has the best unit price in its category.");
        }

        // 8. Optimizarea coșului de cumpărături
        List<String> basket = Arrays.asList("P001", "P005", "P003", "P004");
        Map<Store, List<Product>> allocation = analysisService.optimizeBasket(basket);
        System.out.println("\nShopping basket optimized for minimum cost:");
        double totalCost = 0;
        for (Store store : allocation.keySet()) {
            List<Product> list = allocation.get(store);
            double storeSum = list.stream().mapToDouble(p -> p.getPrice()).sum();
            totalCost += storeSum;
            System.out.println("\n  Store " + store.getName() + " -> " + list.size() + " products, total "
                    + String.format("%.2f", storeSum) + " RON");
            for (Product p : list) {
                System.out.println("     - " + p.getProductName() + " (" + p.getBrand() + ") = "
                        + p.getPrice() + " RON");
            }
        }
        System.out.println("\nTotal basket cost (across all stores): " + String.format("%.2f", totalCost) + " RON");
    }
}
