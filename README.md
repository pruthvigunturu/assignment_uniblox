# Uniblox Ecommerce Assignment

Spring Boot backend API with cart management, checkout, and discount system.

## Quick Start

**Prerequisites**: Java 17+, Maven 3.6+

```bash
mvn clean install
mvn test
mvn spring-boot:run
```

Server runs at: http://localhost:8080

## API

### Add to Cart
```
POST /api/cart/add
{ "userId": "user1", "productId": "prod1", "quantity": 2, "price": 10.0 }
Response: 200 OK - "Item added to cart"
```

### Get Cart
```
GET /api/cart/{userId}
Response: 200 OK - Cart object or 404
```

### Checkout
```
POST /api/checkout
{ "userId": "user1", "discountCode": "UNIК7M3P9QX" }
Response: 200 OK - Order object (or 400 on error)

Order response includes earnedDiscountCode (non-null when this order triggered a reward):
{
  "orderId": "ORD3",
  "userId": "user1",
  "totalAmount": 85.0,
  "discountApplied": 0.0,
  "earnedDiscountCode": "UNIXK2M9PQ3R"
}
```

### Admin: Get Stats
```
GET /api/admin/stats
Response: 200 OK - { totalItemsPurchased, totalRevenue, grossRevenue, totalDiscountCodes, totalDiscountsGiven }
```

### Admin: Generate Discount
```
POST /api/admin/generate-discount
{ "userId": "user1" }
Response: 200 OK or 400 if user not eligible (not at nth order multiple)
```

## Features

- Add items to cart (updates quantity on duplicate)
- Checkout with discount validation (codes are user-bound — only the earner can redeem)
- Auto-generate discount after every nth order (configurable via `AppConstants.NTH_ORDER`)
- Earned discount code returned directly in checkout response
- One-time use discount codes
- Admin: View statistics (items purchased, net revenue, gross revenue, discounts)
- Admin: Generate discount codes for eligible users
- 49 unit tests, 100% pass rate

## Tech Stack

Java 17, Spring Boot 4.0.5, Maven, JUnit 5, Mockito, JaCoCo, ConcurrentHashMap

## Structure

```
controller/  CartController, CheckoutController, AdminController
service/     CartService, OrderService
repository/  CartRepository, OrderRepository, DiscountRepository
model/       Cart, CartItem, Order, Discount
dto/         AddToCartRequest, CheckoutRequest
config/      AppConstants (NTH_ORDER, DISCOUNT_PERCENTAGE, code alphabet)
```

## How It Works

1. Add items to cart
2. Checkout creates order, validates and applies discount if provided
3. Order count derived from actual orders (no separate counter)
4. Every nth order auto-generates a reward discount code
5. Earned code returned in the checkout response so user sees it immediately
6. Discount codes are user-bound — validated at redemption
7. Cart clears after successful checkout

## Testing

```bash
mvn test                    # Run all 49 tests
mvn jacoco:report          # Generate coverage report
```

Coverage: Controllers 100%, Services 100%, Repositories 96%

## Error Codes

- 200: Success
- 400: Empty cart, invalid/used discount code, or code belongs to a different user
- 404: Cart not found
- 500: Server error

See DECISIONS.md for design rationale.
