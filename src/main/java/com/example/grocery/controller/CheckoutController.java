
package com.example.grocery.controller;

import com.example.grocery.dto.BasketRequest;
import com.example.grocery.dto.ItemPriceDto;
import com.example.grocery.dto.ReceiptResponse;
import com.example.grocery.domain.BasketItem;
import com.example.grocery.domain.ItemType;
import com.example.grocery.service.CheckoutService;
import com.example.grocery.service.ItemCatalogService;
import com.example.grocery.util.ItemResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "GroceryItems", description = "Item catalog endpoints: list and manage unit prices")
@RestController
@RequestMapping("/api/v1")
public class CheckoutController {
    @Autowired
    private CheckoutService checkoutService;
    @Autowired
    private ItemCatalogService itemCatalogService;


    @Operation(summary = "GET item types with unit prices", description = "Returns all known ItemType values with their current unit prices.",
            responses = { @ApiResponse(responseCode = "200",description = "Successful retrieval", content = @Content(
            mediaType = "application/json",array = @ArraySchema(schema = @Schema(implementation = ItemPriceDto.class)),examples = {@ExampleObject(name = "items-example", value = """
            [{ "type": "BANANA", "unitPrice": 0.50 },{ "type": "ORANGE", "unitPrice": 0.30 },
            { "type": "APPLE",  "unitPrice": 0.60 },{ "type": "LEMON",  "unitPrice": 0.25 },{ "type": "PEACH",  "unitPrice": 0.75 }]""")} ))
            }
    )
    @GetMapping("/items")
     public ResponseEntity<List<ItemPriceDto>> getItemsWithPrices() {
        var map = itemCatalogService.getAllAsMap();
        var payload = map.entrySet().stream()
                .map(e -> new ItemPriceDto(e.getKey(), e.getValue()))
                .toList();
        return ResponseEntity.ok(payload);
    }

    @Operation(
            summary = "Creates the item checkout receipt",
            description = "Creates the item checkout receipt with discount applied unit price for a given item type",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    description = "Item type and unit price",content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BasketRequest.class),examples = {
                    @ExampleObject(name = "example-request", value = """ 
                    {"items": [{ "item": "Bananas", "quantity": 3 },{ "item": "Oranges", "quantity": 3 },{ "item": "Apples",  "quantity": 1 }]}""")})),
            responses = {
                    @ApiResponse(responseCode = "200",description = "Ok",content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ReceiptResponse.class),examples = {@ExampleObject(name = "example-response", value = """
                        {"items":[{"itemName":"Banana","quantity":3,"amount":1.50},{"itemName":"Orange","quantity":3,"amount":0.90},{"itemName":"Apple","quantity":1,"amount":0.60}],"discounts":[{"description":"Buy 2 Get 1 Free (Bananas)","amount":-0.50},{"description":"3 Oranges for £0.75","amount":-0.15}],"subtotal":3.00,"totalDiscount":-0.65,"total":2.35}""") })),
                    @ApiResponse(responseCode = "400", description = "Invalid item type or price", content = @Content)
            }
    )
    @PostMapping("/checkout")
    public ResponseEntity<ReceiptResponse> checkout(@RequestBody @Valid BasketRequest request) {
        List<BasketItem> basket = request.getItems().stream().map(dto -> {
            ItemType type = ItemResolver.resolve(dto.getItem());
            if (type == null) throw new IllegalArgumentException("Unknown item: " + dto.getItem());
            return BasketItem.builder().type(type).quantity(dto.getQuantity()).build();
        }).toList();
        var receipt = checkoutService.checkout(basket);
        return ResponseEntity.ok(ReceiptResponse.builder()
                .items(receipt.getItems())
                .discounts(receipt.getDiscounts())
                .subtotal(receipt.getSubtotal())
                .totalDiscount(receipt.getTotalDiscount())
                .total(receipt.getTotal())
                .build());
    }
}
