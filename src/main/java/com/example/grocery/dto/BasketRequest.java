
package com.example.grocery.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BasketRequest {
    @NotEmpty
    private List<BasketItemDto> items;
}
