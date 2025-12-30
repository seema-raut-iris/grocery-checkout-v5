
package com.example.grocery.service.promo;

import com.example.grocery.domain.ItemType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class KItemForFixedPriceStrategyTest {

    @Test
    void belowK() {
        var s = new KItemForFixedPriceStrategy(ItemType.ORANGES, 3, new BigDecimal("0.75"));
        var r = s.apply(ItemType.ORANGES, 2, new BigDecimal("0.30"));
        assertThat(r.getAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void groupsDiscount() {
        var s = new KItemForFixedPriceStrategy(ItemType.ORANGES, 3, new BigDecimal("0.75"));
        var r = s.apply(ItemType.ORANGES, 7, new BigDecimal("0.30"));
        assertThat(r.getAmount()).isEqualByComparingTo("0.30"); // 2 groups
        assertThat(r.getDescription()).contains("3 ORANGES");
    }

    @Test
    void supportsOnlyTarget() {
        var s = new KItemForFixedPriceStrategy(ItemType.ORANGES, 3, new BigDecimal("0.75"));
        var r = s.apply(ItemType.BANANAS, 3, new BigDecimal("0.50"));
        assertThat(r.getAmount()).isEqualByComparingTo("0.00");
    }
}
