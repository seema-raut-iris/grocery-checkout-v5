
package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;
import java.math.BigDecimal;
import java.util.Objects;

import static com.example.grocery.util.PriceMath.scale;

/**
 * If quantity >= minQty, use a discounted unit price (for all units).
 * Example: (APPLES, minQty=3, unitPrice=£0.55)
 * Discount = (normalUnitPrice - unitPrice) * quantity
 */
@PromotionType("MIN_QTY_FIXED_UNIT_PRICE")
public class MinQtyFixedUnitPriceStrategy implements DiscountStrategy {

    private final ItemType targetItem;
    private final int minQty;
    private final BigDecimal discountedUnitPrice;
    private final String description;

    // IMPORTANT: keys must match promotions.json params -> { "minQty": "...", "unitPrice": "..." }
    @PromotionCtor(keys = { "minQty", "unitPrice" })
    public MinQtyFixedUnitPriceStrategy(ItemType targetItem, int minQty, BigDecimal unitPrice) {
        if (minQty <= 0) throw new IllegalArgumentException("minQty must be > 0");
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("unitPrice must be positive");
        }
        this.targetItem = Objects.requireNonNull(targetItem);
        this.minQty = minQty;
        this.discountedUnitPrice = scale(unitPrice);
        this.description = targetItem.name() + " unit £" + this.discountedUnitPrice + " (min " + minQty + ")";
    }

    @Override public boolean supports(ItemType type) { return type == targetItem; }

    @Override public int priority() { return 70; }

    @Override
    public DiscountResult apply(ItemType type, int quantity, BigDecimal unitPrice) {
        int required = minQty;
        if (type != targetItem || quantity < required) {
            return new DiscountResult("", BigDecimal.ZERO); // aligns with existing CheckoutService expectations
        }
        BigDecimal delta = scale(unitPrice.subtract(discountedUnitPrice));
        if (delta.compareTo(BigDecimal.ZERO) <= 0) {
            // No discount (already cheaper or equal)
            return new DiscountResult("", BigDecimal.ZERO);
        }
        BigDecimal discount = scale(delta.multiply(BigDecimal.valueOf(quantity)));
        return new DiscountResult(description, discount);
    }

    @Override
    public String name() {
        return targetItem.name().toLowerCase() + "-min-" + minQty + "-unit-" + discountedUnitPrice;
    }
}
