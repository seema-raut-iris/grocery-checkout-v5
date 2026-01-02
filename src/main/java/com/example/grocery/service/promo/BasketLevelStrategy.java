package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;
import com.example.grocery.service.pricing.PriceProvider;
import com.example.grocery.domain.BasketItem;

import java.util.List;
import java.util.Set;


/**
 * Marker + basket-aware apply for promotions that need the whole basket.
 */
public interface BasketLevelStrategy extends DiscountStrategy {

    /**
     * Compute a single basket-wide DiscountResult.
     * Return ZERO amount if not applicable.
     */
    DiscountResult applyBasket(List<BasketItem> basket, PriceProvider priceProvider);

    /** By default, basket promos are exclusive (only one wins). */
    default boolean exclusive() { return true; }


    /**
     * Items affected by this basket promo (e.g., combo items).
     * If empty, interpret as "entire basket".
     */
    default Set<ItemType> affectedItems() { return Set.of(); }


    /** Basket promos are not applied per item in the registry path. */
    @Override
    default boolean supports(ItemType type) { return false; }

    @Override
    default int priority() { return 1000; } // run after item strategies  if ever mixed
}
