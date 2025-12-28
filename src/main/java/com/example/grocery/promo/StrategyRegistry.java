
package com.example.grocery.promo;

import com.example.grocery.domain.ItemType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class StrategyRegistry {
    private final Map<ItemType, List<DiscountStrategy>> byItem = new EnumMap<>(ItemType.class);

    public StrategyRegistry(List<DiscountStrategy> strategies) {
        byItem.put(ItemType.BANANAS, strategies.stream().filter(s -> s.name().contains("bananas")).toList());
        byItem.put(ItemType.ORANGES, strategies.stream().filter(s -> s.name().contains("oranges")).toList());
    }

    public List<DiscountStrategy> strategiesFor(ItemType type) { return byItem.getOrDefault(type, List.of()); }
}
