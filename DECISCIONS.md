# Design Decisions

## Decision: Layered Architecture (Controller → Service → Repository)

**Context:** Need to build a maintainable, testable backend with clear separation of concerns between HTTP handling, business logic, and data access.

**Options Considered:**
- Monolithic approach with all logic in controllers
- Layered architecture (Controller → Service → Repository)
- Event-driven architecture

**Choice:** Layered architecture.

**Why:** Each layer can be tested independently with mocks. Changes to business logic don't affect API contracts. Industry standard for Spring Boot applications. Easy to extend with new features.

---

## Decision: ConcurrentHashMap for In-Memory Storage

**Context:** No database required by assignment. Need thread-safe, simple storage for concurrent requests.

**Options Considered:**
- HashMap (not thread-safe)
- ConcurrentHashMap (thread-safe)
- SQL/NoSQL database

**Choice:** ConcurrentHashMap.

**Why:** Thread-safe for concurrent requests. O(1) average operation time. No external dependencies. Perfect for assignment requirements. Data lost on restart is acceptable for this scope.

---

## Decision: Auto-Generate Discounts on 5th Order

**Context:** Assignment requires reward system where "every nth order gets a coupon code." Need to decide when and how to generate discounts.

**Options Considered:**
- Manual admin generation of discount codes
- Auto-generate during checkout on 5th order
- Batch process discounts periodically

**Choice:** Auto-generate during checkout on 5th order.

**Why:** Automatic with no manual intervention. Reward tied to actual completed orders. Users receive discount immediately after qualifying order. Simple implementation in OrderService.checkout().

---

## Decision: One-Time Use Discount Codes (Global)

**Context:** Need to prevent discount code abuse and ensure fair usage across users.

**Options Considered:**
- Allow unlimited reuse of discount codes
- One-time use per user (track per-user usage)
- Global one-time use with isUsed flag

**Choice:** Global one-time use with isUsed flag.

**Why:** Simple to implement (single boolean flag). Prevents fraud effectively. Clear business logic: "one discount code = one transaction." Aligns with reward system (each 5th order = one discount).

---

## Decision: Comprehensive Unit Testing with Mocks

**Context:** Assignment emphasizes understanding code quality. Need to validate all business logic works correctly.

**Options Considered:**
- No tests
- Only integration tests
- Comprehensive unit tests with mocks

**Choice:** Comprehensive unit testing (37 tests).

**Why:** High code coverage on critical business logic. Fast feedback (2 seconds execution). Tests in isolation using mocks. Easy to identify what breaks when code changes. Demonstrates quality and reduces bugs.

---

## Decision: Discount Code Format (User + Timestamp)

**Context:** Need to generate unique, identifiable discount codes.

**Options Considered:**
- Random alphanumeric strings
- Sequential numbers
- User-based with timestamp
- UUID format

**Choice:** User-based with timestamp (e.g., DISCuser11713046800000).

**Why:** Guaranteed unique (user ID + timestamp combination). Traceable to identify which user earned it. Simple to construct without external libraries. Debuggable for support purposes.

---

## Decision: Null/Empty Discount Code = No Discount

**Context:** Checkout endpoint can receive null or empty discount codes. Need to decide handling.

**Options Considered:**
- Require discount code to be provided
- Treat null/empty as no-discount and proceed
- Reject null/empty discount codes with error

**Choice:** Treat null/empty as no-discount and proceed.

**Why:** User-friendly (customers can checkout without coupon). Not all customers have earned discounts yet. Real-world e-commerce behavior. Simplifies code logic.

---

## Decision: Exception-Based Error Handling

**Context:** Business logic can fail (empty cart, invalid discount). Need to communicate errors to caller.

**Options Considered:**
- Return null values
- Return error codes
- Throw exceptions with messages
- Use Result/Either pattern

**Choice:** Throw meaningful exceptions.

**Why:** Clear what went wrong immediately. Stack traces show exact problem location. Standard Java exception pattern. Easy to test with assertThrows. Controllers map exceptions to appropriate HTTP status codes.

---

## Decision: Update Quantity on Duplicate Items in Cart

**Context:** User adds same product to cart multiple times. Need to decide handling.

**Options Considered:**
- Create duplicate CartItem entries
- Update quantity of existing item
- Reject duplicate with error

**Choice:** Update quantity of existing item.

**Why:** More intuitive UX (users expect quantity to increase). Cart stays lean without duplicates. Real-world shopping behavior. Simple implementation using streams for deduplication.
