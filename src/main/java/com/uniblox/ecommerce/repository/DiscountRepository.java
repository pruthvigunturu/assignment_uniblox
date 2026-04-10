package com.uniblox.ecommerce.repository;

import com.uniblox.ecommerce.model.Discount;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DiscountRepository - Thread-safe discount code storage
 * One-time use: discount.isUsed flag prevents reuse (set by OrderService during checkout)
 */
@Repository
public class DiscountRepository {
    // ConcurrentHashMap ensures thread-safety during concurrent checkout requests
    private final Map<String, Discount> discounts = new ConcurrentHashMap<>();

    public Discount findByCode(String code) {
        return discounts.get(code);
    }

    // Critical: Stores updated discount (with isUsed=true set by OrderService)
    public void save(Discount discount) {
        discounts.put(discount.getCode(), discount);
    }

    public Iterable<Discount> findAll() {
        return discounts.values();
    }
}

