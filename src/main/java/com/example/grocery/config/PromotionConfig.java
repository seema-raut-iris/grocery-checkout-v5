
package com.example.grocery.config;

import com.example.grocery.domain.ItemType;
import com.example.grocery.service.promo.BuyXGetYFreeStrategy;
import com.example.grocery.service.promo.DiscountStrategy;
import com.example.grocery.service.promo.KItemForFixedPriceStrategy;
import com.example.grocery.service.promo.MinQtyFixedUnitPriceStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class PromotionConfig {

    // Bananas: Buy 2 Get 1 Free
    @Bean
    public DiscountStrategy bananasB2G1() {
        return new BuyXGetYFreeStrategy(ItemType.BANANAS, 2, 1);
    }

    // Oranges: 3 for £0.75
    @Bean
    public DiscountStrategy oranges3For075() {
        return new KItemForFixedPriceStrategy(ItemType.ORANGES, 3, new BigDecimal("0.75"));
    }

    // Apples: unit price £0.55 if quantity >= 3
    @Bean
    public DiscountStrategy applesMin3Unit55() {
        return new MinQtyFixedUnitPriceStrategy(ItemType.APPLES, 3, new BigDecimal("0.55"));
    }
}
