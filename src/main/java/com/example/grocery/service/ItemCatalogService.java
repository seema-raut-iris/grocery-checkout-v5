
// src/main/java/com/example/grocery/service/ItemCatalogService.java
package com.example.grocery.service;

import com.example.grocery.domain.ItemType;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Service contract for exposing item catalog and pricing snapshots.
 */
public interface ItemCatalogService {
    /**
     * Returns a snapshot of all item prices from the configured PriceProvider.
     */
    Map<ItemType, BigDecimal> getAllAsMap();
}
