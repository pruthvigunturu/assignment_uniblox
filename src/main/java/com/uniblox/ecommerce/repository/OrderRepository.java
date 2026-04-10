package com.uniblox.ecommerce.repository;

import com.uniblox.ecommerce.model.Order;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class OrderRepository {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong orderIdCounter = new AtomicLong(1);

    public Order save(Order order) {
        if (order.getOrderId() == null) {
            order.setOrderId("ORD" + orderIdCounter.getAndIncrement());
        }
        orders.put(order.getOrderId(), order);
        return order;
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public Order findById(String orderId) {
        return orders.get(orderId);
    }
}