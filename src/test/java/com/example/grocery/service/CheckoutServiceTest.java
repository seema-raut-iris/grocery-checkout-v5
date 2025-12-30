
package com.example.grocery.service;

import com.example.grocery.domain.BasketItem;
import com.example.grocery.domain.ItemType;
import com.example.grocery.domain.Receipt;
import com.example.grocery.service.pricing.PriceProvider;
import com.example.grocery.service.promo.BuyXGetYFreeStrategy;
import com.example.grocery.service.promo.KItemForFixedPriceStrategy;
import com.example.grocery.service.promo.MinQtyFixedUnitPriceStrategy;
import com.example.grocery.service.promo.StrategyRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CheckoutService tests without Mockito:
 * - Real PriceProvider (in-test implementation)
 * - Real StrategyRegistry
 * - Real promotion strategies
 */
class CheckoutServiceTest {

    /** In-test PriceProvider using the default unit prices from requirements. */
    private static class TestPriceProvider implements PriceProvider {
        @Override
        public BigDecimal priceOf(ItemType type) {
            return switch (type) {
                case BANANAS -> new BigDecimal("0.50");
                case ORANGES -> new BigDecimal("0.30");
                case APPLES  -> new BigDecimal("0.60");
                case LEMONS  -> new BigDecimal("0.25");
                case PEACHES -> new BigDecimal("0.75");
            };
        }
    }

    /** Common registry for tests with standard promotions. */
    private StrategyRegistry buildStandardRegistry() {
        return new StrategyRegistry(List.of(
                // Bananas: Buy 2 Get 1 Free
                new BuyXGetYFreeStrategy(ItemType.BANANAS, 2, 1),
                // Oranges: 3 for £0.75
                new KItemForFixedPriceStrategy(ItemType.ORANGES, 3, new BigDecimal("0.75")),
                // Apples: unit £0.55 when qty >= 3
                new MinQtyFixedUnitPriceStrategy(ItemType.APPLES, 3, new BigDecimal("0.55"))
        ));
    }

    @Test
    @DisplayName("Example basket: 3 Bananas, 3 Oranges, 1 Apple → subtotal £3.00; discounts -£0.65; total £2.35")
    void exampleBasket() {
        var priceProvider = new TestPriceProvider();
        var registry = buildStandardRegistry();
        var checkoutService = new CheckoutService(priceProvider, registry);

        Receipt receipt = checkoutService.checkout(List.of(
                BasketItem.builder().type(ItemType.BANANAS).quantity(3).build(),
                BasketItem.builder().type(ItemType.ORANGES).quantity(3).build(),
                BasketItem.builder().type(ItemType.APPLES).quantity(1).build()
        ));

        // Items
        assertThat(receipt.getItems()).hasSize(3);

        // Subtotal: (3×0.50)+(3×0.30)+(1×0.60) = 1.50+0.90+0.60 = 3.00
        assertThat(receipt.getSubtotal()).isEqualByComparingTo("3.00");

        // Discounts:
        // - Bananas B2G1 → 1 free × 0.50 = 0.50
        // - Oranges 3 for £0.75 → (0.90 - 0.75) = 0.15
        // Total discount: -(0.50 + 0.15) = -0.65
        assertThat(receipt.getTotalDiscount()).isEqualByComparingTo("-0.65");

        // Total: 3.00 - 0.65 = 2.35
        assertThat(receipt.getTotal()).isEqualByComparingTo("2.35");

        // Ensure discount descriptions exist

        assertThat(receipt.getDiscounts())
                .extracting(com.example.grocery.domain.DiscountLine::getDescription) // String
                .anyMatch(desc -> desc.contains("Buy 2 Get 1 Free"));

        assertThat(receipt.getDiscounts())
                .extracting(com.example.grocery.domain.DiscountLine::getDescription)
                .anyMatch(desc -> desc.contains("3 ORANGES for £0.75"));

    }

    @Test
    @DisplayName("No discount applicable: Peaches only → discount line with 0.00; totals unchanged")
    void noDiscountFallback() {
        var priceProvider = new TestPriceProvider();
        // Registry with standard promotions (none for peaches)
        var registry = buildStandardRegistry();
        var checkoutService = new CheckoutService(priceProvider, registry);

        Receipt receipt = checkoutService.checkout(List.of(
                BasketItem.builder().type(ItemType.PEACHES).quantity(2).build()
        ));

        // Fallback discount line
        assertThat(receipt.getDiscounts()).hasSize(1);
        assertThat(receipt.getDiscounts().get(0).getDescription()).isEqualTo("No Discount Applicable");
        assertThat(receipt.getDiscounts().get(0).getAmount()).isEqualByComparingTo("0.00");

        // Subtotal: 2×0.75 = 1.50; Total discount: 0.00; Total: 1.50
        assertThat(receipt.getSubtotal()).isEqualByComparingTo("1.50");
        assertThat(receipt.getTotalDiscount()).isEqualByComparingTo("0.00");
        assertThat(receipt.getTotal()).isEqualByComparingTo("1.50");
    }

    @Test
    @DisplayName("Apples min-qty discounted unit price (≥3): qty=4 → discount £0.20; total £2.20")
    void applesMinQtyUnitPrice() {
        var priceProvider = new TestPriceProvider();
        var registry = buildStandardRegistry();
        var checkoutService = new CheckoutService(priceProvider, registry);

        Receipt receipt = checkoutService.checkout(List.of(
                BasketItem.builder().type(ItemType.APPLES).quantity(4).build()
        ));

        // Subtotal: 0.60 × 4 = 2.40
        assertThat(receipt.getSubtotal()).isEqualByComparingTo("2.40");
        // Discount: (0.60 - 0.55) × 4 = 0.20 → shown as -0.20
        assertThat(receipt.getTotalDiscount()).isEqualByComparingTo("-0.20");
        // Total: 2.20
        assertThat(receipt.getTotal()).isEqualByComparingTo("2.20");
    }
}
