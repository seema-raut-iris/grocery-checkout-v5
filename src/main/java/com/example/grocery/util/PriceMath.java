
package com.example.grocery.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PriceMath {
    private PriceMath() {}

    public static BigDecimal scale(BigDecimal bd) {
        return bd.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return scale(a.add(b));
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return scale(a.subtract(b));
    }

    public static BigDecimal multiply(BigDecimal a, int qty) {
        return scale(a.multiply(BigDecimal.valueOf(qty)));
    }

    public static BigDecimal negate(BigDecimal a) {
        return scale(a.negate());
    }
}
