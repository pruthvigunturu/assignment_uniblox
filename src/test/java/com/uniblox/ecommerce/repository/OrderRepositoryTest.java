package com.uniblox.ecommerce.repository;

import com.uniblox.ecommerce.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository = new OrderRepository();
    }

    @Test
    void save_NewOrder_ShouldPersistAndRetrieve() {
        // Arrange
        Order order = new Order();
        order.setOrderId("ORDER1");
        order.setUserId("user1");
        order.setItems(new ArrayList<>());
        order.setTotalAmount(100.0);

        // Act
        orderRepository.save(order);

        // Assert
        Order saved = orderRepository.findById("ORDER1");
        assertNotNull(saved);
        assertEquals("user1", saved.getUserId());
        assertEquals(100.0, saved.getTotalAmount());
    }

    @Test
    void findById_NonExistingOrder_ShouldReturnNull() {
        // Act & Assert
        assertNull(orderRepository.findById("NONEXISTENT"));
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        // Arrange
        Order order1 = new Order();
        order1.setOrderId("ORDER1");
        order1.setUserId("user1");

        Order order2 = new Order();
        order2.setOrderId("ORDER2");
        order2.setUserId("user2");

        orderRepository.save(order1);
        orderRepository.save(order2);

        // Act
        java.util.List<Order> result = orderRepository.findAll();

        // Assert
        assertEquals(2, result.size());
    }
}

