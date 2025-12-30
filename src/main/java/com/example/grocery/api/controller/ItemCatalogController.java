
package com.example.grocery.api.controller;

import com.example.grocery.api.dto.ItemPriceDto;
import com.example.grocery.service.ItemCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "GroceryItems", description = "Item catalog endpoints: list and manage unit prices")
@RestController
@RequestMapping("/api/v1/items")
public class ItemCatalogController {

    @Autowired
    private ItemCatalogService itemCatalogService;

    @Operation(
            summary = "Get item types with unit prices",
            description = "Returns all known item types with current unit prices.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful retrieval",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ItemPriceDto.class)),
                                    examples = {
                                            @ExampleObject(
                                                    name = "items-example",
                                                    value = """
                            [
                              {"type":"BANANA","unitPrice":0.50},
                              {"type":"ORANGE","unitPrice":0.30},
                              {"type":"APPLE","unitPrice":0.60},
                              {"type":"LEMON","unitPrice":0.25},
                              {"type":"PEACH","unitPrice":0.75}
                            ]
                            """
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<List<ItemPriceDto>> getItemsWithPrices() {
        var map = itemCatalogService.getAllAsMap();
        var payload = map.entrySet().stream()
                .map(e -> new ItemPriceDto(e.getKey(), e.getValue()))
                .toList();
        return ResponseEntity.ok(payload);
    }
}
