package com.uniblox.ecommerce.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private String userId;
    private String discountCode;
}