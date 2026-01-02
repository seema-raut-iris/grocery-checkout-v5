
// src/main/java/com/example/grocery/service/CheckoutService.java
package com.example.grocery.service;

import com.example.grocery.domain.BasketItem;
import com.example.grocery.domain.Receipt;
import java.util.List;

/**
 * Service contract for checkout operations.
 */
public interface CheckoutService {
    /**
     * Calculates an itemized receipt for the given basket, applying
     * per-item and basket-level promotions based on configured strategies.
     *
     * @param basket list of BasketItem entries
     * @return Receipt with items, discounts, subtotal, and total
     */
    Receipt checkout(List<BasketItem> basket);
}
