package com.uniblox.ecommerce.service;

import com.uniblox.ecommerce.dto.CheckoutRequest;
import com.uniblox.ecommerce.model.*;
import com.uniblox.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OrderService - Core checkout and discount logic
 *
 * Key Responsibilities:
 * 1. Process checkout: validate cart, apply discount, create order
 * 2. Manage user order count for reward eligibility
 * 3. Auto-generate discounts on every 5th order
 */
@Service
public class OrderService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private UserRepository userRepository;

    // REWARD SYSTEM: Every 5th order gets a 10% discount
    private static final int NTH_ORDER = 5;
    private static final double DISCOUNT_PERCENTAGE = 10.0;

    /**
     * Process checkout: create order from cart, validate/apply discount, track user orders, generate rewards
     *
     * Flow:
     * 1. Validate cart exists and has items → 400 if empty
     * 2. Calculate total from all cart items
     * 3. If discount code provided: validate (must be unused) → 400 if invalid
     * 4. Create order with discount applied
     * 5. Increment user order count
     * 6. Check if user qualifies for reward (every 5th order)
     * 7. Clear cart after successful checkout
     */
    public Order checkout(CheckoutRequest request) {
        // CRITICAL: Validate cart before proceeding
        Cart cart = cartRepository.findByUserId(request.getUserId());
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Calculate order total from all items (price * quantity per item)
        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // CRITICAL: Discount validation - one-time use only
        double discountApplied = 0.0;
        if (request.getDiscountCode() != null && !request.getDiscountCode().isEmpty()) {
            Discount discount = discountRepository.findByCode(request.getDiscountCode());
            // Must exist AND not be used previously
            if (discount != null && !discount.isUsed()) {
                discountApplied = totalAmount * (discount.getPercentage() / 100.0);
                discount.setUsed(true); // Mark as used to prevent reuse
                discountRepository.save(discount);
            } else {
                throw new IllegalArgumentException("Invalid or used discount code");
            }
        }

        double finalAmount = totalAmount - discountApplied;

        // Create order record with applied discount
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setItems(cart.getItems());
        order.setTotalAmount(finalAmount);
        order.setDiscountApplied(discountApplied);
        orderRepository.save(order);

        // REWARD SYSTEM: Track user order count for 5th order reward
        User user = userRepository.findByUserId(request.getUserId());
        user.setOrderCount(user.getOrderCount() + 1);
        userRepository.save(user);

        // CRITICAL: Check if eligible for discount reward (every 5th order)
        if (user.getOrderCount() % NTH_ORDER == 0) {
            generateDiscountForUser(request.getUserId());
        }

        // IMPORTANT: Clear cart after successful checkout to prevent duplicate orders
        cartRepository.deleteByUserId(request.getUserId());

        return order;
    }

    /**
     * Generate discount code for user (called after 5th order).
     * Format: DISC<userId><timestamp> ensures uniqueness and traceability
     */
    public void generateDiscountForUser(String userId) {
        Discount discount = new Discount();
        // Unique code: userId + timestamp guarantees no collisions
        discount.setCode("DISC" + userId + System.currentTimeMillis());
        discount.setPercentage(DISCOUNT_PERCENTAGE);
        discount.setUsed(false);
        discountRepository.save(discount);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Iterable<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }
}

