package com.accesa.price_comparator.controller;

import com.accesa.price_comparator.model.Discount;
import com.accesa.price_comparator.service.PriceAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final PriceAnalysisService analysisService;

    public DiscountController(PriceAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // 1. The highest active discounts at the current moment (top N, default is 3)
    @GetMapping("/best")
    public List<Discount> getTopDiscounts(@RequestParam(name="top", defaultValue = "3") int top) {
        return analysisService.getBestDiscounts(top);
    }

    // 2. Discounts added in the last 24 hours
    @GetMapping("/new")
    public List<Discount> getNewDiscounts() {
        return analysisService.getNewDiscounts();
    }
}
