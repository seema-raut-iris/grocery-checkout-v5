
// src/main/java/com/example/grocery/config/dto/PromotionFile.java
package com.example.grocery.api.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Getter
@Setter
public class PromotionFileDTO {
    private List<PromotionRuleDTO> promotions;
}
