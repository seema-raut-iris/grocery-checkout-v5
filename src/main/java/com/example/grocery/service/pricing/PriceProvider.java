
package com.example.grocery.service.pricing;

import com.example.grocery.domain.ItemType;

import java.math.BigDecimal;

public interface PriceProvider {
    BigDecimal priceOf(ItemType type);
}
