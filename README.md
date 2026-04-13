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
{ "userId": "user1", "discountCode": "DISC10" }
Response: 200 OK - Order object (or 400 on error)
```

### Admin: Get Stats
```
GET /api/admin/stats
Response: 200 OK - { totalItemsPurchased, totalRevenue, totalDiscountCodes, totalDiscountsGiven }
```

### Admin: Generate Discount
```
POST /api/admin/generate-discount
{ "userId": "user1" }
Response: 200 OK or 400 if user not eligible (not at 5th order multiple)
```

## Features

- Add items to cart (updates quantity on duplicate)
- Checkout with discount validation
- Auto-generate 10% discount after 5th order
- One-time use discount codes
- Admin: View statistics (items, revenue, discounts)
- Admin: Generate discount codes for eligible users
- ~50 unit tests (100% pass rate)

## Tech Stack

Java 17, Spring Boot 4.0.5, Maven, JUnit 5, Mockito, JaCoCo, ConcurrentHashMap

## Structure

```
controller/  CartController, CheckoutController, AdminController
service/     CartService, OrderService
repository/  CartRepository, OrderRepository, UserRepository, DiscountRepository
model/       Cart, CartItem, Order, User, Discount
dto/         AddToCartRequest, CheckoutRequest
```

## How It Works

1. Add items to cart
2. Checkout creates order, validates discount, increments user order count
3. Every 5th order generates a 10% discount code
4. Discount codes are one-time use
5. Cart clears after successful checkout

## Testing

```bash
mvn test                    # Run all 37 tests
mvn jacoco:report          # Generate coverage report
```

Coverage: Controllers 100%, Services 100%, Repositories 96%

## Error Codes

- 400: Empty cart or invalid discount code
- 404: Cart not found
- 500: Server error

See DECISIONS.md for design rationale.
