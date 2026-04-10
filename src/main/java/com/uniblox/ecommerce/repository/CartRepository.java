package com.uniblox.ecommerce.repository;

import com.uniblox.ecommerce.model.Cart;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * CartRepository - In-memory cart storage
 * WARNING: Uses HashMap (NOT thread-safe). See UserRepository for thread-safe pattern with ConcurrentHashMap.
 *
 * This design choice was intentional for this assignment but in production,
 * should use ConcurrentHashMap or a database for concurrent safety.
 */
@Repository
public class CartRepository {
    // IMPORTANT: HashMap is not thread-safe; multiple concurrent requests could cause issues
    // Production systems should use ConcurrentHashMap or database
    private final Map<String, Cart> carts = new HashMap<>();

    public Cart findByUserId(String userId) {
        return carts.get(userId);
    }

    public void save(Cart cart) {
        carts.put(cart.getUserId(), cart);
    }

    public void deleteByUserId(String userId) {
        carts.remove(userId);
    }
}

