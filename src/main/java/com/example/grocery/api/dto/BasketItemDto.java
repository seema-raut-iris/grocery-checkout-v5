
package com.example.grocery.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BasketItemDto {
    @NotBlank
    private String item;
    @Min(1)
    private int quantity;
}
