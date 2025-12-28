
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
        LOOKUP.put(standardize("banana"), ItemType.BANANA);
        LOOKUP.put(standardize("bananas"), ItemType.BANANA);
        LOOKUP.put(standardize("orange"), ItemType.ORANGE);
        LOOKUP.put(standardize("oranges"), ItemType.ORANGE);
        LOOKUP.put(standardize("apple"), ItemType.APPLE);
        LOOKUP.put(standardize("apples"), ItemType.APPLE);
        LOOKUP.put(standardize("lemon"), ItemType.LEMON);
        LOOKUP.put(standardize("lemons"), ItemType.LEMON);
        LOOKUP.put(standardize("peach"), ItemType.PEACH);
        LOOKUP.put(standardize("peaches"), ItemType.PEACH);
    }

    private static String standardize(String s) { return s.trim().toLowerCase(Locale.ROOT); }

    public static ItemType resolve(String input) { return LOOKUP.get(standardize(input)); }
}
