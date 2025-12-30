
package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MinQtyFixedUnitPriceStrategyTest {

    @Test
    void belowMinQty() {
        var s = new MinQtyFixedUnitPriceStrategy(ItemType.APPLES, 3, new BigDecimal("0.55"));
        var r = s.apply(ItemType.APPLES, 2, new BigDecimal("0.60"));
        assertThat(r.getAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void discountedUnitApplies() {
        var s = new MinQtyFixedUnitPriceStrategy(ItemType.APPLES, 3, new BigDecimal("0.55"));
        var r = s.apply(ItemType.APPLES, 4, new BigDecimal("0.60"));
        assertThat(r.getAmount()).isEqualByComparingTo("0.20");
        assertThat(r.getDescription()).contains("unit £0.55");
    }
}
