
# Grocery Checkout — Java 17 + Spring Boot 3.3.4 + Lombok 1.18.40

Production-ready structure with **SOLID** design, **Actuator**, **Strategy pattern** for promotions, and documentation (ER + UML diagrams). Built to compile cleanly with Lombok.

## Run
```bash
mvn clean package
mvn spring-boot:run
```
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Actuator: `http://localhost:8080/actuator` (health, info, metrics, prometheus, etc.)
- API: `GET /api/v1/items`, `POST /api/v1/checkout`

## Assumptions
- The set of `ItemType` values (Banana, Orange, Apple, Lemon, Peach) is **final** and does not change.
- Prices for these items are considered stable and managed internally.
- Therefore, **PUT** (update) and **DELETE** endpoints for items are **not required** in this implementation.
- Only **GET** (to retrieve item types and prices) and **POST** (to calculate basket totals) endpoints are provided.

## API GET /api/v1/items Example Request
```bash
curl --location --request GET 'http://localhost:8080/api/v1/items'
```

##  API POST /api/v1/checkout Example Request:
```bash
curl --location --request POST 'http://localhost:8080/api/v1/checkout' \
--header 'Content-Type: application/json' \
--data-raw '{
  "items": [
    { "item": "Bananas", "quantity": 7 },
    { "item": "Oranges", "quantity": 10 },
    { "item": "Apples",  "quantity": 1 }
  ]
}'
```
### Example Output 
```
{"items": [
    {"itemName":"Banana","quantity":7,"amount":3.50},
    {"itemName":"Orange","quantity":10,"amount":3.00},
    {"itemName":"Apple","quantity":1,"amount":0.60}
    ],
"discounts":[
    {"description":"Buy 2 Get 1 Free (Bananas)","amount":-1.00},
    {"description":"3 Oranges for £0.75","amount":-0.45}
    ],
"subtotal":7.10,
"totalDiscount":-1.45,
"total":5.65
}
```

## Promotions
- Bananas: **Buy 2, get 1 free**.
- Oranges: **3 for £0.75**.

## Architecture & SOLID
- **SRP**: pricing, promos, checkout aggregation, API layers.
- **OCP**: add new promos via `DiscountStrategy`, no service changes.
- **LSP**: strategies honor a single contract.
- **ISP**: DTOs isolated from domain.
- **DIP**: `CheckoutService` depends on `PriceProvider` & `StrategyRegistry`.

## Observability & Health
- **Actuator** exposes health, info, metrics, and Prometheus endpoint.

## Error Handling
- Global `@RestControllerAdvice` returns RFC-7807 `ProblemDetail` responses for validation and domain errors.

## Diagrams (Mermaid)
See files under `diagrams/`.
