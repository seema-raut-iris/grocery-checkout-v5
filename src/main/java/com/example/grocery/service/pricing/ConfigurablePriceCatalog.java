package com.example.grocery.service.pricing;

import com.example.grocery.domain.ItemType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static com.example.grocery.util.PriceMath.scale;

@Component
@EnableConfigurationProperties(PriceCatalogProperties.class)
public class ConfigurablePriceCatalog implements PriceProvider {

    private final Map<ItemType, BigDecimal> prices = new EnumMap<>(ItemType.class);

    public ConfigurablePriceCatalog(PriceCatalogProperties props) {
        // 1) Load from application.yml (catalog.prices)
        if (props.getPrices() != null) {
            props.getPrices().forEach((k, v) -> prices.put(k, scale(v)));
        }
        System.out.println("Default Items Price :"+prices);
        // 2) Optional JSON override (classpath: prices.json)
        try {
            ClassPathResource json = new ClassPathResource("prices.json");
            if (json.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json.getInputStream());
                JsonNode node = root.get("prices");
                if (node != null && node.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> e = fields.next();
                        ItemType item = ItemType.valueOf(e.getKey().trim().toUpperCase());
                        BigDecimal val = scale(new BigDecimal(e.getValue().asText()));
                        prices.put(item, val); // override
                    }
                    System.out.println("Updated Items Price from price.json:"+prices);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load prices.json", ex);
        }
    }

    @Override
    public BigDecimal priceOf(ItemType type) {
        return Optional.ofNullable(prices.get(type))
                .orElseThrow(() -> new IllegalArgumentException("Missing price for item: " + type));
    }
}
