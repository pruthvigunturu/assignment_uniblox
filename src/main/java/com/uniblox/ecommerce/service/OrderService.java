package com.uniblox.ecommerce.service;

import com.uniblox.ecommerce.AppConstants;
import com.uniblox.ecommerce.dto.CheckoutRequest;
import com.uniblox.ecommerce.model.*;
import com.uniblox.ecommerce.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DiscountRepository discountRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    public Order checkout(CheckoutRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId());
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double discountApplied = 0.0;
        if (request.getDiscountCode() != null && !request.getDiscountCode().isEmpty()) {
            Discount discount = discountRepository.findByCode(request.getDiscountCode());
            if (discount == null || discount.isUsed()) {
                throw new IllegalArgumentException("Invalid or used discount code");
            }
            if (discount.getUserId() != null && !discount.getUserId().equals(request.getUserId())) {
                throw new IllegalArgumentException("Discount code does not belong to this user");
            }
            discountApplied = totalAmount * (discount.getPercentage() / 100.0);
            discount.setUsed(true);
            discountRepository.save(discount);
        }

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setItems(cart.getItems());
        order.setTotalAmount(totalAmount - discountApplied);
        order.setDiscountApplied(discountApplied);
        orderRepository.save(order);

        long orderCount = orderRepository.findAll().stream()
                .filter(o -> request.getUserId().equals(o.getUserId()))
                .count();

        if (orderCount > 0 && orderCount % AppConstants.NTH_ORDER == 0) {
            try {
                String earnedCode = generateDiscountForUser(request.getUserId());
                order.setEarnedDiscountCode(earnedCode);
            } catch (Exception e) {
                log.error("Coupon generation failed for user {} after order — will need manual retry", request.getUserId(), e);
            }
        }

        cartRepository.deleteByUserId(request.getUserId());

        return order;
    }

    public String generateDiscountForUser(String userId) {
        for (int attempt = 0; attempt < 3; attempt++) {
            StringBuilder sb = new StringBuilder(AppConstants.DISCOUNT_CODE_PREFIX);
            for (int i = 0; i < AppConstants.DISCOUNT_CODE_LENGTH; i++) {
                sb.append(AppConstants.DISCOUNT_CODE_ALPHABET.charAt(
                        RANDOM.nextInt(AppConstants.DISCOUNT_CODE_ALPHABET.length())));
            }
            String code = sb.toString();
            Discount discount = new Discount();
            discount.setCode(code);
            discount.setUserId(userId);
            discount.setPercentage(AppConstants.DISCOUNT_PERCENTAGE);
            discount.setUsed(false);
            if (discountRepository.saveIfAbsent(discount)) {
                return code;
            }
            log.warn("Discount code collision on attempt {} for user {}, retrying", attempt + 1, userId);
        }
        throw new RuntimeException("Failed to generate unique discount code after 3 attempts");
    }

    public long getIssuedDiscountCountForUser(String userId) {
        return discountRepository.countByUserId(userId);
    }

    public long getOrderCountForUser(String userId) {
        return orderRepository.findAll().stream()
                .filter(o -> userId.equals(o.getUserId()))
                .count();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Iterable<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }
}
