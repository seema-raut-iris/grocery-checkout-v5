
// src/main/java/com/example/grocery/service/promo/PromotionType.java
package com.example.grocery.service.promo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;

/** Declares the external ruleType key used in promotions.json. */
@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
public @interface PromotionType {
    String value(); // e.g. "BUY_X_GET_Y_FREE"
}
