package com.uniblox.ecommerce.model;

import lombok.Data;

import java.util.List;

@Data
public class Order {
    private String orderId;
    private String userId;
    private List<CartItem> items;
    private double totalAmount;
    private double discountApplied;
}