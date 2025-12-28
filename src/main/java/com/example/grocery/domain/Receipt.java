
package com.example.grocery.domain;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class Receipt {
    @Singular List<ReceiptLine> items;
    @Singular List<DiscountLine> discounts;
    BigDecimal subtotal;
    BigDecimal totalDiscount; // negative value
    BigDecimal total;
}
