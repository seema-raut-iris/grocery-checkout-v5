
package com.example.grocery.service.impl;

import com.example.grocery.domain.ItemType;
import com.example.grocery.service.ItemCatalogService;
import com.example.grocery.service.pricing.PriceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Service
public class ItemCatalogServiceImpl implements ItemCatalogService {

    @Autowired
    private PriceProvider priceProvider;

    public Map<ItemType, BigDecimal> getAllAsMap() {
        Map<ItemType, BigDecimal> map = new EnumMap<>(ItemType.class);
        for (ItemType type : ItemType.values()) {
            map.put(type, priceProvider.priceOf(type));
        }
        System.out.println("Items and price :"+map);
        return map;
    }

    /*public List<ItemType> getAllTypes() {
        return List.of(ItemType.values());
    }

    public List<BigDecimal> getAllPrices() {
        return getAllTypes().stream().map(priceProvider::priceOf).collect(Collectors.toList());
    }*/
}
