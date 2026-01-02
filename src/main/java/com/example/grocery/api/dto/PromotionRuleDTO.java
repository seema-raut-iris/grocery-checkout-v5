package com.example.grocery.api.dto;

import com.example.grocery.domain.ItemType;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PromotionRuleDTO {
    @JsonAlias("type")
    private String ruleType;
    private ItemType itemType;           // required for item-scoped strategies
    private Map<String, String> params;  // flexible parameter bag

    }
