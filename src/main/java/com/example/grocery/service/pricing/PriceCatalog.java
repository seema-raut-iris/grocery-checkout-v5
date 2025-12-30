
package com.example.grocery.service.pricing;

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
        prices.put(ItemType.BANANAS, scale(new BigDecimal("0.50")));
        prices.put(ItemType.ORANGES, scale(new BigDecimal("0.30")));
        prices.put(ItemType.APPLES,  scale(new BigDecimal("0.60")));
        prices.put(ItemType.LEMONS,  scale(new BigDecimal("0.25")));
        prices.put(ItemType.PEACHES,  scale(new BigDecimal("0.75")));
    }

    @Override
    public BigDecimal priceOf(ItemType type) {
        return prices.get(type);
    }
}
