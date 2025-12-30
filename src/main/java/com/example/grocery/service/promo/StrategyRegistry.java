
package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Builds a per-item, ordered list of strategies using supports() and priority().
 */
@Component
public class StrategyRegistry {

    private final Map<ItemType, List<DiscountStrategy>> byItem = new EnumMap<>(ItemType.class);

    public StrategyRegistry(List<DiscountStrategy> strategies) {
        for (ItemType item : ItemType.values()) {
            List<DiscountStrategy> list = strategies.stream()
                    .filter(s -> s.supports(item))
                    .sorted(Comparator.comparingInt(DiscountStrategy::priority))
                    .toList();
            byItem.put(item, list);
        }
    }

    public List<DiscountStrategy> strategiesFor(ItemType type) {
        return byItem.getOrDefault(type, List.of());
    }
}
