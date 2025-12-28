
package com.example.grocery.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ReceiptLine {
    String itemName;
    int quantity;
    BigDecimal amount;
}
