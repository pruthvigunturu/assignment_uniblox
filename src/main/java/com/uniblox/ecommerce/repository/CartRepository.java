package com.uniblox.ecommerce.repository;

import com.uniblox.ecommerce.model.Cart;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CartRepository {
    private final Map<String, Cart> carts = new ConcurrentHashMap<>();

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

