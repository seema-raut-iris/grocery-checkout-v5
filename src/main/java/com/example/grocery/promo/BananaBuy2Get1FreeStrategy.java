
package com.example.grocery.promo;

import com.example.grocery.domain.ItemType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

import static com.example.grocery.util.PriceMath.multiply;

@Component
public class BananaBuy2Get1FreeStrategy implements DiscountStrategy {
    @Override
    public DiscountResult apply(ItemType type, int quantity, BigDecimal unitPrice) {
        if (type != ItemType.BANANA || quantity < 3) return new DiscountResult("", BigDecimal.ZERO);
        int free = quantity / 3; // each trio -> 1 free
        BigDecimal discount = multiply(unitPrice, free);
        return new DiscountResult("Buy 2 Get 1 Free (Bananas)", discount);
    }
    @Override public String name() { return "bananas-b2g1"; }
}
