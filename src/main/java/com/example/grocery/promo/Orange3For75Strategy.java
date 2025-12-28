
package com.example.grocery.promo;

import com.example.grocery.domain.ItemType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.example.grocery.util.PriceMath.scale;

@Component
public class Orange3For75Strategy implements DiscountStrategy {
    @Override
    public DiscountResult apply(ItemType type, int quantity, BigDecimal unitPrice) {
        if (type != ItemType.ORANGE || quantity < 3) return new DiscountResult("", BigDecimal.ZERO);
        int trios = quantity / 3;

        BigDecimal regularPerTrio = scale(unitPrice.multiply(new BigDecimal("3")));
        BigDecimal promoPerTrio   = scale(new BigDecimal("0.75"));
        BigDecimal perTrioDiscount = scale(regularPerTrio.subtract(promoPerTrio));
        BigDecimal discount = scale(perTrioDiscount.multiply(new BigDecimal(trios)));

        return new DiscountResult("3 Oranges for £0.75", discount);
    }
    @Override public String name() { return "oranges-3-for-75"; }
}
