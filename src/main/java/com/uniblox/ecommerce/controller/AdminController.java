package com.uniblox.ecommerce.controller;

import com.uniblox.ecommerce.AppConstants;
import com.uniblox.ecommerce.model.CartItem;
import com.uniblox.ecommerce.model.Discount;
import com.uniblox.ecommerce.model.Order;
import com.uniblox.ecommerce.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<Order> orders = orderService.getAllOrders();
        Iterable<Discount> discounts = orderService.getAllDiscounts();

        int totalItemsPurchased = orders.stream()
                .mapToInt(order -> order.getItems().stream().mapToInt(CartItem::getQuantity).sum())
                .sum();

        double totalRevenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();

        long totalDiscountCodes = 0;
        for (Discount d : discounts) {
            totalDiscountCodes++;
        }

        double totalDiscountsGiven = orders.stream().mapToDouble(Order::getDiscountApplied).sum();
        double grossRevenue = totalRevenue + totalDiscountsGiven;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItemsPurchased", totalItemsPurchased);
        stats.put("totalRevenue", totalRevenue);
        stats.put("grossRevenue", grossRevenue);
        stats.put("totalDiscountCodes", totalDiscountCodes);
        stats.put("totalDiscountsGiven", totalDiscountsGiven);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/generate-discount")
    public ResponseEntity<String> generateDiscount(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        long orderCount = orderService.getOrderCountForUser(userId);
        long entitledCodes = orderCount / AppConstants.NTH_ORDER;
        long issuedCodes = orderService.getIssuedDiscountCountForUser(userId);

        if (entitledCodes == 0) {
            return ResponseEntity.badRequest().body("User not eligible for discount");
        }
        if (issuedCodes >= entitledCodes) {
            return ResponseEntity.badRequest().body("Discount already issued for this milestone");
        }
        orderService.generateDiscountForUser(userId);
        return ResponseEntity.ok("Discount generated for user " + userId);
    }
}
