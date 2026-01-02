
// src/main/java/com/example/grocery/service/promo/PromotionCtor.java
package com.example.grocery.service.promo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.CONSTRUCTOR;

/** Declares param keys (order) that map to constructor arguments after ItemType. */
@Retention(RetentionPolicy.RUNTIME)
@Target(CONSTRUCTOR)
public @interface PromotionCtor {
    String[] keys();
}
