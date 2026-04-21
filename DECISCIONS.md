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

**Choice:** ConcurrentHashMap

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

## Decision: Discount Code Format (Prefix + SecureRandom)

**Context:** Need to generate unique, unguessable discount codes that are safe for users to type manually.

**Options Considered:**
- User ID + timestamp (predictable — an attacker who knows userId and approximate generation time can enumerate codes)
- UUID (36 chars, contains ambiguous lowercase + hyphens, designed for machine IDs not human entry)
- Sequential ID encoded with Hashids/Sqids (reversible encoding, adds a third-party dependency)
- Prefix + SecureRandom over a curated alphabet

**Choice:** `UNI` prefix + 8 cryptographically random characters from a custom alphabet.

**Why:** `SecureRandom` is cryptographically unpredictable — unlike `Random` it cannot be seeded and brute-forced. The custom alphabet (`ABCDEFGHJKLMNPQRSTUVWXYZ23456789`) excludes ambiguous characters (I, l, 1, O, 0) so users can type codes without confusion. 32^8 ≈ 1 trillion combinations makes enumeration infeasible. No external dependency required. Not reversible — there is no encoding to decode.

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

## Decision: User-Bound Discount Codes

**Context:** Discount codes are generated as rewards for a specific user completing their nth order. Decide whether codes should be tied to that user or usable by anyone.

**Options Considered:**
- Global codes — any user can apply any valid code (simple, but codes can be shared or stolen)
- User-bound codes — validate that the code's owner matches the user at checkout

**Choice:** User-bound codes. Each generated code stores the `userId` of the earner and is validated against the requesting user at checkout.

**Why:** A reward program loses integrity if codes are shareable. User binding ensures only the user who earned the code can redeem it, preventing code sharing, leaking, or scraping. Implementation cost is minimal: one field on `Discount`, one equality check in `OrderService.checkout()`.

---

## Decision: Calculate Order Count On-The-Fly from Orders

**Context:** The nth-order reward system needs to know how many orders a user has placed. Decide whether to maintain a counter on the User object or derive the count from stored orders.

**Options Considered:**
- Store `orderCount` on a `User` model, increment on each checkout
- Derive count by filtering `OrderRepository.findAll()` by userId at checkout time

**Choice:** Derive on-the-fly from `OrderRepository`.

**Why:** A stored counter is a second source of truth that can diverge from reality — if the counter increment succeeds but the order save fails (or vice versa), the count is wrong and either a reward is missed or incorrectly triggered. Deriving from actual orders is always consistent. For an in-memory store with modest order volume, the O(n) scan is negligible. This also eliminates the `User` model and `UserRepository` entirely, reducing complexity.

---

## Decision: Update Quantity on Duplicate Items in Cart

**Context:** User adds same product to cart multiple times. Need to decide handling.

**Options Considered:**
- Create duplicate CartItem entries
- Update quantity of existing item
- Reject duplicate with error

**Choice:** Update quantity of existing item.

**Why:** More intuitive UX (users expect quantity to increase). Cart stays lean without duplicates. Real-world shopping behavior. Simple implementation using streams for deduplication.
