package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;
import com.example.grocery.service.pricing.PriceProvider;
import com.example.grocery.domain.BasketItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static com.example.grocery.util.PriceMath.scale;

/**
 * Basket-level: percent off if subtotal >= threshold, optionally capped.
 *
 * @PromotionType: "MAX_SUBTOTAL_PERCENT_OFF"
 * @PromotionCtor keys:
 *   threshold -> minimum basket subtotal to apply (e.g. "1000.00")
 *   percent   -> percent off (0..100) (e.g. "10.0")
 *   cap       -> optional max discount amount (e.g. "200.00")
 */
@PromotionType("MAX_SUBTOTAL_PERCENT_OFF")
public class MaxSubtotalPercentOffStrategy implements BasketLevelStrategy {

    private final BigDecimal threshold;
    private final BigDecimal percent; // 0..100
    private final BigDecimal cap;      // nullable

    @PromotionCtor(keys = { "threshold", "percent", "cap" })
    public MaxSubtotalPercentOffStrategy(BigDecimal threshold, BigDecimal percent, String capOpt) {
        this.threshold = scale(threshold);
        this.percent = scale(percent);
        this.cap = (capOpt == null || capOpt.isBlank()) ? null : scale(new BigDecimal(capOpt));

        if (this.threshold.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("threshold >= 0");
        if (this.percent.compareTo(BigDecimal.ZERO) < 0 || this.percent.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("percent must be between 0 and 100");
        if (this.cap != null && this.cap.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("cap must be > 0 when provided");
    }

    @Override
    public DiscountResult apply(ItemType type, int quantity, BigDecimal unitPrice) {
        // Basket strategy: no per-item application
        return new DiscountResult("", BigDecimal.ZERO);
    }


    @Override
    public Set<ItemType> affectedItems() {
        return Set.of(); // empty == entire basket
    }


    @Override
    public DiscountResult applyBasket(List<BasketItem> basket, PriceProvider priceProvider) {
        if (basket == null || basket.isEmpty()) return new DiscountResult("", BigDecimal.ZERO);

        BigDecimal subtotal = basket.stream()
                .map(it -> scale(priceProvider.priceOf(it.getType()))
                        .multiply(BigDecimal.valueOf(it.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (subtotal.compareTo(threshold) < 0) return new DiscountResult("", BigDecimal.ZERO);

        BigDecimal raw = scale(subtotal.multiply(percent).divide(new BigDecimal("100")));
        BigDecimal discount = (cap == null) ? raw : raw.min(cap);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) return new DiscountResult("", BigDecimal.ZERO);

        String description = percent + "% off on subtotal >= " + threshold + (cap != null ? " (cap " + cap + ")" : "");
        return new DiscountResult(description, discount);
    }

    @Override
    public String name() {
        return "MAX_SUBTOTAL_PERCENT_OFF(threshold=" + threshold + ", percent=" + percent + (cap != null ? ", cap=" + cap : "") + ")";
    }
}
