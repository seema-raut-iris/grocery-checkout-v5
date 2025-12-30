
package com.example.grocery.service;

import com.example.grocery.domain.*;
import com.example.grocery.service.pricing.PriceProvider;
import com.example.grocery.service.promo.DiscountResult;
import com.example.grocery.service.promo.DiscountStrategy;
import com.example.grocery.service.promo.StrategyRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.grocery.util.PriceMath.*;

/**
 * CheckoutService:
 *  - Computes line totals from unit price * quantity
 *  - Applies all eligible discount strategies per item (via StrategyRegistry)
 *  - Builds an itemized receipt with discounts, subtotal, and grand total
 */
@Service
public class CheckoutService {


    private final PriceProvider priceProvider;

    private final StrategyRegistry registry;

    public CheckoutService(PriceProvider priceProvider, StrategyRegistry registry) {
        this.priceProvider = priceProvider;
        this.registry = registry;
    }

    /**
     * Calculate an itemized receipt for the given basket.
     */
    public Receipt checkout(List<BasketItem> basket) {
        List<ReceiptLine> itemLines = new ArrayList<>();
        List<DiscountLine> discountLines = new ArrayList<>();

        BigDecimal subtotal = scale(BigDecimal.ZERO);
        BigDecimal totalDiscount = scale(BigDecimal.ZERO);

        for (BasketItem item : basket) {
            // 1) Unit price for the item (from dynamic/DB-backed provider)
            BigDecimal unit = scale(priceProvider.priceOf(item.getType()));
            // 2) Line price (unit * quantity)
            BigDecimal linePrice = multiply(unit, item.getQuantity());

            // Add item line to receipt
            itemLines.add(
                    ReceiptLine.builder()
                            .itemName(item.getType().name().toLowerCase())
                            .quantity(item.getQuantity())
                            .amount(linePrice)
                            .build()
            );
            subtotal = add(subtotal, linePrice);

            // 3) Apply all eligible strategies for this item type (ordered by priority)
            for (DiscountStrategy strategy : registry.strategiesFor(item.getType())) {
                DiscountResult result = strategy.apply(item.getType(), item.getQuantity(), unit);

                // Only record positive discounts
                if (result.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    // Receipt shows discount lines as negative amounts
                    discountLines.add(
                            DiscountLine.builder()
                                    .description(result.getDescription())
                                    .amount(negate(result.getAmount()))
                                    .build()
                    );
                    totalDiscount = add(totalDiscount, result.getAmount());
                }
            }
        }


// 🔁 Fallback: if no discounts anywhere, add a single informational line
        if (discountLines.isEmpty()) {
            discountLines.add(
                    DiscountLine.builder()
                            .description("No Discount Applicable")
                            .amount(scale(BigDecimal.ZERO))
                            .build()
            );
        }

        // 4) Final totals
        BigDecimal total = subtract(subtotal, totalDiscount);

        return Receipt.builder()
                .items(itemLines)
                .discounts(discountLines)
                .subtotal(subtotal)
                // totalDiscount shown as negative in the receipt model
                .totalDiscount(negate(totalDiscount))
                .total(total)
                .build();
    }
}
