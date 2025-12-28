
package com.example.grocery.util;

import com.example.grocery.domain.ItemType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ItemResolver {
    private static final Map<String, ItemType> LOOKUP = new HashMap<>();
    static {
        for (ItemType it : ItemType.values()) {
            LOOKUP.put(standardize(it.name()), it);
        }
        LOOKUP.put(standardize("banana"), ItemType.BANANAS);
        LOOKUP.put(standardize("bananas"), ItemType.BANANAS);
        LOOKUP.put(standardize("orange"), ItemType.ORANGES);
        LOOKUP.put(standardize("oranges"), ItemType.ORANGES);
        LOOKUP.put(standardize("apple"), ItemType.APPLES);
        LOOKUP.put(standardize("apples"), ItemType.APPLES);
        LOOKUP.put(standardize("lemon"), ItemType.LEMONS);
        LOOKUP.put(standardize("lemons"), ItemType.LEMONS);
        LOOKUP.put(standardize("peach"), ItemType.PEACHES);
        LOOKUP.put(standardize("peaches"), ItemType.PEACHES);
    }

    private static String standardize(String s) { return s.trim().toLowerCase(Locale.ROOT); }

    public static ItemType resolve(String input) { return LOOKUP.get(standardize(input)); }
}
