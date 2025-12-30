
package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;

import java.math.BigDecimal;
import java.util.Objects;

import static com.example.grocery.util.PriceMath.scale;

/**
 * Generic "K for fixed price" promotion, applicable to ANY ItemType.
 * Example: (ORANGES, 3, £0.75) => "3 ORANGES for £0.75".
 *
 * Discount for complete groups of K:
 *   perGroupDiscount = (unitPrice * K) - groupPrice
 *   totalDiscount    = perGroupDiscount * groups
 */
public class KItemForFixedPriceStrategy implements DiscountStrategy {

    private final ItemType targetItem;
    private final int k;
    private final BigDecimal groupPrice;
    private final String description;

    public KItemForFixedPriceStrategy(ItemType targetItem, int k, BigDecimal groupPrice) {
        if (k <= 1) throw new IllegalArgumentException("k must be > 1");
        if (groupPrice == null || groupPrice.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("groupPrice must be positive");

        this.targetItem = Objects.requireNonNull(targetItem);
        this.k = k;
        this.groupPrice = scale(groupPrice);
        this.description = k + " " + targetItem.name() + " for £" + this.groupPrice;
    }

    @Override
    public boolean supports(ItemType type) {
        return type == targetItem;
    }

    @Override
    public int priority() {
        return 60;
    }

    @Override
    public DiscountResult apply(ItemType type, int quantity, BigDecimal unitPrice) {
        if (type != targetItem || quantity < k) {
            return new DiscountResult("", BigDecimal.ZERO);
        }
        int groups = quantity / k;

        BigDecimal regularForGroups = scale(unitPrice.multiply(BigDecimal.valueOf(k))
                .multiply(BigDecimal.valueOf(groups)));
        BigDecimal promoForGroups = scale(groupPrice.multiply(BigDecimal.valueOf(groups)));

        BigDecimal discount = scale(regularForGroups.subtract(promoForGroups));
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return new DiscountResult("", BigDecimal.ZERO);
        }
        return new DiscountResult(description, discount);
    }

    @Override
    public String name() {
        return targetItem.name().toLowerCase() + "-" + k + "-for-" + groupPrice;
    }

    ;
}
