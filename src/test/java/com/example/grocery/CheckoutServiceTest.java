
package com.example.grocery;

import com.example.grocery.domain.BasketItem;
import com.example.grocery.domain.ItemType;
import com.example.grocery.domain.Receipt;
import com.example.grocery.pricing.PriceCatalog;
import com.example.grocery.promo.BananaBuy2Get1FreeStrategy;
import com.example.grocery.promo.Orange3For75Strategy;
import com.example.grocery.promo.StrategyRegistry;
import com.example.grocery.service.CheckoutService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CheckoutServiceTest {

    @InjectMocks
    public CheckoutService service;

    @Test
    void exampleBasket() {
        /*CheckoutService service = new CheckoutService(new PriceCatalog(), new StrategyRegistry(List.of(
                new BananaBuy2Get1FreeStrategy(), new Orange3For75Strategy()
        )));*/
        Receipt r = service.checkout(List.of(
                BasketItem.builder().type(ItemType.BANANA).quantity(3).build(),
                BasketItem.builder().type(ItemType.ORANGE).quantity(4).build(),
                BasketItem.builder().type(ItemType.APPLE).quantity(1).build()
        ));

        assertEquals("£3.30", r.getSubtotal().toString());
        // Banana discount: £0.50; Orange discount: £0.15
        assertEquals("£-0.65", r.getTotalDiscount().toString());
        assertEquals("£2.65", r.getTotal().toString());
    }
}
