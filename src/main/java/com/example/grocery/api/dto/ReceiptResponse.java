
package com.example.grocery.api.dto;

import com.example.grocery.domain.DiscountLine;
import com.example.grocery.domain.ReceiptLine;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class ReceiptResponse {
    List<ReceiptLine> items;
    List<DiscountLine> discounts;
    BigDecimal subtotal;
    BigDecimal totalDiscount;
    BigDecimal total;
}
