package com.uniblox.ecommerce.model;

import lombok.Data;

@Data
public class Discount {
    private String code;
    private double percentage;
    private boolean isUsed;
}