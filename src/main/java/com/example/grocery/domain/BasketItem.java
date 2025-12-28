
package com.example.grocery.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BasketItem {
    ItemType type;
    int quantity;
}
