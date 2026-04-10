package com.uniblox.ecommerce.repository;

import com.uniblox.ecommerce.model.User;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserRepository - Thread-safe user order tracking
 * Uses computeIfAbsent() for atomic create-if-absent (prevents race conditions)
 */
@Repository
public class UserRepository {
    // ConcurrentHashMap handles concurrent checkout requests safely
    private final Map<String, User> users = new ConcurrentHashMap<>();

    // Critical: Atomic operation - only one thread creates user (order counting accuracy)
    public User findByUserId(String userId) {
        return users.computeIfAbsent(userId, id -> {
            User user = new User();
            user.setUserId(id);
            user.setOrderCount(0);
            return user;
        });
    }

    public void save(User user) {
        users.put(user.getUserId(), user);
    }
}

