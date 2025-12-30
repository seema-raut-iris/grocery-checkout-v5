
package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;
import java.math.BigDecimal;

public interface DiscountStrategy {
    DiscountResult apply(ItemType type, int quantity, BigDecimal unitPrice);
    String name();

    /** Strategy can specify which item(s) it supports. Defaults to 'true' for all. */
    default boolean supports(ItemType type) { return true; }

    /** Lower priority runs earlier. Use to control stacking order if needed. */
    default int priority() { return 100; }
}
