package com.uniblox.ecommerce.util;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {

    private static final int MAX_REQUESTS = 4;
    private static final long WINDOW_MS = 10_000;

    private final ConcurrentHashMap<String, ArrayDeque<Long>> timestamps = new ConcurrentHashMap<>();

    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        ArrayDeque<Long> deque = timestamps.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            while (!deque.isEmpty() && now - deque.peekFirst() > WINDOW_MS) {
                deque.pollFirst();
            }
            if (deque.size() < MAX_REQUESTS) {
                deque.addLast(now);
                return true;
            }
            return false;
        }
    }
}