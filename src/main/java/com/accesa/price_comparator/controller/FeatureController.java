package com.accesa.price_comparator.controller;

import com.accesa.price_comparator.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
/*
• Daily Shopping Basket Monitoring:
o Help users split their basket into shopping lists that optimise for cost savings
• Best Discounts:
o List products with the highest current percentage discounts across all tracked
stores.
• New Discounts:
o List discounts that have been newly added (e.g., within the last 24 hours)

 */
@RestController
public class FeatureController {

    @Autowired
    private ProductService productService;


    @GetMapping()
    public void getDailyShoppingBasket(){

    }

    @GetMapping()
    public void getBestDiscounts(){

    }

}
