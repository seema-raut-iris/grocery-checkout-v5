
package com.example.grocery.dto;

import com.example.grocery.domain.ItemType;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class ItemPriceDto {
    ItemType item;
    BigDecimal unitPrice;
}
