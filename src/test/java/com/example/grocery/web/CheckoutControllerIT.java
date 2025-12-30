
package com.example.grocery.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CheckoutController using the real application context.
 * No mocks—verifies endpoint behavior and common response fields.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CheckoutControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/checkout → returns itemized receipt with discounts array")
    void checkoutReturnsReceiptStructure() throws Exception {
        String payload = """
        {"items":[
          {"item":"Bananas","quantity":3},
          {"item":"Oranges","quantity":3},
          {"item":"Apples","quantity":1}
        ]}
        """;

        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                // root fields
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.discounts").isArray())
                .andExpect(jsonPath("$.subtotal").exists())
                .andExpect(jsonPath("$.totalDiscount").exists())
                .andExpect(jsonPath("$.total").exists())
                // item lines (domain model usually uses itemName, quantity, amount)
                .andExpect(jsonPath("$.items[0].itemName").exists())
                .andExpect(jsonPath("$.items[0].quantity").exists())
                .andExpect(jsonPath("$.items[0].amount").exists());
    }

    @Test
    @DisplayName("POST /api/v1/checkout → example basket totals (Bananas×3, Oranges×3, Apples×1)")
    void checkoutComputesExpectedTotalsForExample() throws Exception {
        String payload = """
        {"items":[
          {"item":"Bananas","quantity":3},
          {"item":"Oranges","quantity":3},
          {"item":"Apples","quantity":1}
        ]}
        """;

        // These assertions match the standard rules:
        // Bananas: Buy 2 Get 1 Free (0.50 off) | Oranges: 3 for £0.75 (0.15 off) | Subtotal=3.00 | TotalDiscount=-0.65 | Total=2.35
        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(3.00))
                .andExpect(jsonPath("$.totalDiscount").value(-0.65))
                .andExpect(jsonPath("$.total").value(2.35));
    }

    @Test
    @DisplayName("POST /api/v1/checkout → no discounts case shows 'No discount applicable'")
    void checkoutNoDiscountsShowsMessage() throws Exception {
        String payload = """
        {"items":[
          {"item":"Peaches","quantity":2}
        ]}
        """;

        mockMvc.perform(post("/api/v1/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discounts[0].description").value("No Discount Applicable"))
                .andExpect(jsonPath("$.discounts[0].amount").value(0.00));
    }
}
