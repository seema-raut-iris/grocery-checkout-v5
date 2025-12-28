
package com.example.grocery.pricing;

import com.example.grocery.domain.ItemType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static com.example.grocery.util.PriceMath.scale;

@Component
public class PriceCatalog implements PriceProvider {
    private final Map<ItemType, BigDecimal> prices = new EnumMap<>(ItemType.class);

    public PriceCatalog() {
        prices.put(ItemType.BANANA, scale(new BigDecimal("0.50")));
        prices.put(ItemType.ORANGE, scale(new BigDecimal("0.30")));
        prices.put(ItemType.APPLE,  scale(new BigDecimal("0.60")));
        prices.put(ItemType.LEMON,  scale(new BigDecimal("0.25")));
        prices.put(ItemType.PEACH,  scale(new BigDecimal("0.75")));
    }

    @Override
    public BigDecimal priceOf(ItemType type) {
        return prices.get(type);
    }
}
