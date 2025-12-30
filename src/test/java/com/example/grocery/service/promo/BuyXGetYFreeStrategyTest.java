
package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BuyXGetYFreeStrategyTest {

    @Test
    void belowThreshold() {
        var s = new BuyXGetYFreeStrategy(ItemType.BANANAS, 2, 1);
        var r = s.apply(ItemType.BANANAS, 2, new BigDecimal("0.50"));
        assertThat(r.getAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void groupsDiscount() {
        var s = new BuyXGetYFreeStrategy(ItemType.BANANAS, 2, 1);
        var r = s.apply(ItemType.BANANAS, 7, new BigDecimal("0.50"));
        assertThat(r.getAmount()).isEqualByComparingTo("1.00");
        assertThat(r.getDescription()).contains("Buy 2 Get 1 Free");
    }

    @Test
    void supportsOnlyTarget() {
        var s = new BuyXGetYFreeStrategy(ItemType.BANANAS, 2, 1);
        var r = s.apply(ItemType.ORANGES, 6, new BigDecimal("0.30"));
        assertThat(r.getAmount()).isEqualByComparingTo("0.00");
    }
}

