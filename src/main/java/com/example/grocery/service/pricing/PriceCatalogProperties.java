package com.example.grocery.service.pricing;

import com.example.grocery.domain.ItemType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "catalog")
public class PriceCatalogProperties {

    /**
     * Map of item -> price from application.yml.
     * Example:
     * catalog:
     *   prices:
     *     BANANAS: 0.50
     *     ORANGES: 0.30
     */
    @NotNull
    private Map<ItemType, BigDecimal> prices = new EnumMap<>(ItemType.class);

}
