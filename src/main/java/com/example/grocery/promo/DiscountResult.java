
package com.example.grocery.promo;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class DiscountResult {
    String description;
    BigDecimal amount; // positive discount amount (we’ll negate at display time)
}
