
package com.example.grocery.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class DiscountLine {
    String description;
    BigDecimal amount; // convention: negative for display (e.g., -0.50)
}
