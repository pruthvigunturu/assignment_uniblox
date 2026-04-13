package com.uniblox.ecommerce.controller;

import com.uniblox.ecommerce.model.Discount;
import com.uniblox.ecommerce.model.Order;
import com.uniblox.ecommerce.model.User;
import com.uniblox.ecommerce.repository.UserRepository;
import com.uniblox.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    private static final int NTH_ORDER = 5;
    private static final double DISCOUNT_PERCENTAGE = 10.0;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Order> orders = orderService.getAllOrders();
        Iterable<Discount> discounts = orderService.getAllDiscounts();

        int totalItemsPurchased = orders.stream()
                .mapToInt(order -> order.getItems().stream().mapToInt(item -> item.getQuantity()).sum())
                .sum();

        double totalRevenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();

        long totalDiscountCodes = 0;
        for (Discount d : discounts) {
            totalDiscountCodes++;
        }

        double totalDiscountsGiven = orders.stream().mapToDouble(Order::getDiscountApplied).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItemsPurchased", totalItemsPurchased);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalDiscountCodes", totalDiscountCodes);
        stats.put("totalDiscountsGiven", totalDiscountsGiven);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/generate-discount")
    public ResponseEntity<String> generateDiscount(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        User user = userRepository.findByUserId(userId);
        if (user.getOrderCount() % NTH_ORDER == 0 && user.getOrderCount() > 0) {
            orderService.generateDiscountForUser(userId);
            return ResponseEntity.ok("Discount generated for user " + userId);
        } else {
            return ResponseEntity.badRequest().body("User not eligible for discount");
        }
    }
}