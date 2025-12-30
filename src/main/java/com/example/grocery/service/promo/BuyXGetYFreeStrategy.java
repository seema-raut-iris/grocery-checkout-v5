
package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;

import java.math.BigDecimal;
import java.util.Objects;

import static com.example.grocery.util.PriceMath.scale;

/**
 * Generic "Buy X Get Y Free" promotion, applicable to ANY ItemType.
 * Example: (BANANAS, 2, 1) => "Buy 2 Get 1 Free (BANANAS)".
 *
 * For each full group of (x + y) items, Y units are free.
 */
public class BuyXGetYFreeStrategy implements DiscountStrategy {

    private final ItemType targetItem;
    private final int x;
    private final int y;
    private final String description;

    public BuyXGetYFreeStrategy(ItemType targetItem, int x, int y) {
        if (x <= 0 || y <= 0) throw new IllegalArgumentException("x,y must be > 0");
        this.targetItem = Objects.requireNonNull(targetItem);
        this.x = x;
        this.y = y;
        this.description = "Buy " + x + " Get " + y + " Free (" + targetItem.name() + ")";
    }

    @Override public boolean supports(ItemType type) { return type == targetItem; }
    @Override public int priority() { return 50; }

    @Override
    public DiscountResult apply(ItemType type, int quantity, BigDecimal unitPrice) {
        int groupSize = x + y;
        if (type != targetItem || quantity < groupSize) {
            return new DiscountResult("", BigDecimal.ZERO);
        }
        int groups = quantity / groupSize;
        int freeUnits = groups * y;

        BigDecimal discount = scale(unitPrice.multiply(BigDecimal.valueOf(freeUnits)));
        return new DiscountResult(description, discount);
    }

    @Override public String name() { return targetItem.name().toLowerCase() + "-b" + x + "g" + y; }
}
