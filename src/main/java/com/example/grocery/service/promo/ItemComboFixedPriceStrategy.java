package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;
import com.example.grocery.service.pricing.PriceProvider;
import com.example.grocery.domain.BasketItem;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.grocery.util.PriceMath.scale;

/**
 * Basket-level combo: specified items and quantities for a fixed price per combo.
 *
 * @PromotionType: "ITEM_COMBO_FIXED_PRICE"
 * @PromotionCtor keys:
 *   combo  -> CSV "APPLES:1,BANANAS:2,ORANGES:1"
 *   price  -> fixed total price per combo set (e.g. "1.25")
 *   max    -> optional cap on number of combo sets applied (e.g. "3"). If absent, unlimited.
 */
@PromotionType("ITEM_COMBO_FIXED_PRICE")
public class ItemComboFixedPriceStrategy implements BasketLevelStrategy {

    private final Map<ItemType, Integer> combo;
    private final BigDecimal comboPrice;
    private final Integer maxSets; // nullable

    @PromotionCtor(keys = { "combo", "price", "max" })
    public ItemComboFixedPriceStrategy(String comboCsv, BigDecimal price, String maxOpt) {
        this.combo = parseComboCsv(comboCsv);
        this.comboPrice = scale(price);
        this.maxSets = (maxOpt == null || maxOpt.isBlank()) ? null : Integer.parseInt(maxOpt);
        if (combo.isEmpty()) throw new IllegalArgumentException("combo must define at least one item:qty");
        if (comboPrice.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("price must be > 0");
        if (maxSets != null && maxSets <= 0) throw new IllegalArgumentException("max must be > 0");
    }

    @Override
    public DiscountResult apply(ItemType type, int quantity, BigDecimal unitPrice) {
        // Basket strategy: no per-item application
        return new DiscountResult("", BigDecimal.ZERO);
    }


    @Override
    public Set<ItemType> affectedItems() {
        return combo.keySet(); // APPLES, BANANAS, etc.
    }


    @Override
    public DiscountResult applyBasket(List<BasketItem> basket, PriceProvider priceProvider) {
        if (basket == null || basket.isEmpty()) return new DiscountResult("", BigDecimal.ZERO);

        // Compute how many full combo sets fit in the basket
        Map<ItemType, Integer> counts = basket.stream()
                .collect(Collectors.toMap(BasketItem::getType, BasketItem::getQuantity, Integer::sum));

        int possibleSets = combo.entrySet().stream()
                .mapToInt(e -> counts.getOrDefault(e.getKey(), 0) / e.getValue())
                .min().orElse(0);

        if (possibleSets <= 0) return new DiscountResult("", BigDecimal.ZERO);
        int sets = (maxSets == null) ? possibleSets : Math.min(possibleSets, maxSets);

        // Normal price of one combo
        BigDecimal normalOneSet = combo.entrySet().stream()
                .map(e -> scale(priceProvider.priceOf(e.getKey()))
                        .multiply(BigDecimal.valueOf(e.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountPerSet = normalOneSet.subtract(comboPrice);
        if (discountPerSet.compareTo(BigDecimal.ZERO) <= 0) return new DiscountResult("", BigDecimal.ZERO);

        BigDecimal totalDiscount = scale(discountPerSet.multiply(BigDecimal.valueOf(sets)));
        String description = "Combo " + formatCombo(combo) + " for " + comboPrice + " (x" + sets + ")";
        return new DiscountResult(description, totalDiscount);
    }

    @Override
    public String name() {
        return "ITEM_COMBO_FIXED_PRICE(" + formatCombo(combo) + " -> " + comboPrice +
                (maxSets != null ? ", max=" + maxSets : "") + ")";
    }

    private static Map<ItemType, Integer> parseComboCsv(String csv) {
        if (csv == null) return Map.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(
                        a -> ItemType.valueOf(a[0].trim().toUpperCase()),
                        a -> Integer.parseInt(a[1].trim())
                ));
    }

    private static String formatCombo(Map<ItemType, Integer> combo) {
        return combo.entrySet().stream()
                .map(e -> e.getKey().name() + ":" + e.getValue())
                .collect(Collectors.joining(","));
    }
}
