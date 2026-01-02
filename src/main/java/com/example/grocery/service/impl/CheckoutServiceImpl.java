
// src/main/java/com/example/grocery/service/CheckoutService.java
package com.example.grocery.service.impl;

import com.example.grocery.domain.*;
import com.example.grocery.service.pricing.PriceProvider;
import com.example.grocery.service.promo.BasketLevelStrategy;
import com.example.grocery.service.promo.DiscountResult;
import com.example.grocery.service.promo.DiscountStrategy;
import com.example.grocery.service.promo.StrategyRegistry;
import org.springframework.stereotype.Service;

import com.example.grocery.service.CheckoutService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.grocery.util.PriceMath.*;


/**
 * Default implementation of the CheckoutService.
 */
@Service
public class CheckoutServiceImpl implements CheckoutService {
    private final PriceProvider priceProvider;
    private final StrategyRegistry registry;
    private final List<BasketLevelStrategy> basketStrategies;

    public CheckoutServiceImpl(PriceProvider priceProvider,
                               StrategyRegistry registry,
                               List<BasketLevelStrategy> basketStrategies) {
        this.priceProvider = priceProvider;
        this.registry = registry;
        this.basketStrategies = basketStrategies;
    }

    public Receipt checkout(List<BasketItem> basket) {
        List<ReceiptLine> itemLines = new ArrayList<>();
        List<DiscountLine> discountLines = new ArrayList<>();
        BigDecimal subtotal = scale(BigDecimal.ZERO);
        BigDecimal totalDiscount = scale(BigDecimal.ZERO);

        // Compute & record item lines + collect per-item strategies to apply
        Map<ItemType, BigDecimal> unitPrices = new EnumMap<>(ItemType.class);
        Map<ItemType, Integer> quantities = new EnumMap<>(ItemType.class);
        for (BasketItem item : basket) {
            BigDecimal unit = scale(priceProvider.priceOf(item.getType()));
            unitPrices.put(item.getType(), unit);
            quantities.put(item.getType(), item.getQuantity());

            BigDecimal linePrice = multiply(unit, item.getQuantity());
            itemLines.add(ReceiptLine.builder()
                    .itemName(item.getType().name().toLowerCase())
                    .quantity(item.getQuantity())
                    .amount(linePrice)
                    .build());
            subtotal = add(subtotal, linePrice);
        }

        // ---- SINGLE BUCKET EXCLUSIVE POLICY ----

        // 1) Evaluate all basket strategies once
        record BasketCandidate(BasketLevelStrategy strategy, DiscountResult result) {}
        List<BasketCandidate> candidates = basketStrategies.stream()
                .map(s -> new BasketCandidate(s, s.applyBasket(basket, priceProvider)))
                .filter(c -> c.result().getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        // 2) Pick the single best basket promo (highest amount) among exclusive ones
        Optional<BasketCandidate> bestExclusive = candidates.stream()
                .filter(c -> c.strategy().exclusive())
                .max(Comparator.comparing(c -> c.result().getAmount()));

        Set<ItemType> suppressedItems = Set.of();
        if (bestExclusive.isPresent()) {
            BasketCandidate chosen = bestExclusive.get();
            // Record the basket discount
            discountLines.add(DiscountLine.builder()
                    .description(chosen.result().getDescription())
                    .amount(negate(chosen.result().getAmount()))
                    .build());
            totalDiscount = add(totalDiscount, chosen.result().getAmount());

            // Suppress item-level discounts for affected items
            Set<ItemType> affected = chosen.strategy().affectedItems();
            suppressedItems = affected.isEmpty()
                    ? quantities.keySet()                 // empty => whole basket suppressed
                    : affected;
        }

        // ---- Apply item-level strategies except suppressed items ----
        for (ItemType type : quantities.keySet()) {
            if (suppressedItems.contains(type)) continue; // policy: skip per-item for affected items

            BigDecimal unit = unitPrices.get(type);
            int qty = quantities.get(type);

            for (DiscountStrategy strategy : registry.strategiesFor(type)) {
                DiscountResult result = strategy.apply(type, qty, unit);
                if (result.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    discountLines.add(DiscountLine.builder()
                            .description(result.getDescription())
                            .amount(negate(result.getAmount()))
                            .build());
                    totalDiscount = add(totalDiscount, result.getAmount());
                }
            }
        }

        if (discountLines.isEmpty()) {
            discountLines.add(DiscountLine.builder()
                    .description("No Discount Applicable")
                    .amount(scale(BigDecimal.ZERO))
                    .build());
        }

        BigDecimal total = subtract(subtotal, totalDiscount);
        return Receipt.builder()
                .items(itemLines)
                .discounts(discountLines)
                .subtotal(subtotal)
                .totalDiscount(negate(totalDiscount))
                .total(total)
                .build();
    }
}
