package com.uniblox.ecommerce.controller;

import com.uniblox.ecommerce.dto.CheckoutRequest;
import com.uniblox.ecommerce.model.Order;
import com.uniblox.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    // Critical: Service throws IllegalArgumentException for empty cart or invalid discount - Spring converts to 400
    public ResponseEntity<Order> checkout(@RequestBody CheckoutRequest request) {
        Order order = orderService.checkout(request);
        return ResponseEntity.ok(order);
    }
}

