package com.uniblox.ecommerce.dto;

import lombok.Data;

@Data
public class AddToCartRequest {
    private String userId;
    private String productId;
    private int quantity;
    private double price;
}
