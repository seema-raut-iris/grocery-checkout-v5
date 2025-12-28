
package com.example.grocery.promo;

import com.example.grocery.domain.ItemType;
import java.math.BigDecimal;

public interface DiscountStrategy {
    DiscountResult apply(ItemType type, int quantity, BigDecimal unitPrice);
    String name();
}
