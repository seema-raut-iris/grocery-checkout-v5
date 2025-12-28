
package com.example.grocery.service;

import com.example.grocery.domain.*;
import com.example.grocery.pricing.PriceProvider;
import com.example.grocery.promo.DiscountResult;
import com.example.grocery.promo.DiscountStrategy;
import com.example.grocery.promo.StrategyRegistry;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.grocery.util.PriceMath.*;

@Service
public class CheckoutService {
    private final PriceProvider priceProvider;
    private final StrategyRegistry registry;

    public CheckoutService(PriceProvider priceProvider, StrategyRegistry registry) {
        this.priceProvider = priceProvider;
        this.registry = registry;
    }

    public Receipt checkout(List<BasketItem> basket) {
        List<ReceiptLine> itemLines = new ArrayList<>();
        List<DiscountLine> discountLines = new ArrayList<>();

        BigDecimal subtotal = scale(BigDecimal.ZERO);
        BigDecimal totalDiscount = scale(BigDecimal.ZERO);

        for (BasketItem item : basket) {
            BigDecimal unit = priceProvider.priceOf(item.getType());
            BigDecimal linePrice = multiply(unit, item.getQuantity());

            itemLines.add(ReceiptLine.builder()
                    .itemName(item.getType().name().substring(0,1) + item.getType().name().substring(1).toLowerCase())
                    .quantity(item.getQuantity())
                    .amount(linePrice)
                    .build());

            subtotal = add(subtotal, linePrice);

            for (DiscountStrategy strategy : registry.strategiesFor(item.getType())) {
                DiscountResult result = strategy.apply(item.getType(), item.getQuantity(), unit);
                if (result.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    // display as negative
                    discountLines.add(DiscountLine.builder()
                            .description(result.getDescription())
                            .amount(negate(result.getAmount()))
                            .build());
                    totalDiscount = add(totalDiscount, result.getAmount());
                }
            }
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
