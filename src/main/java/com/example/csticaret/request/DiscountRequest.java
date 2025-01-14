package com.example.csticaret.request;

import lombok.Data;

@Data
public class DiscountRequest {
    private Long productId;
    private Double discountRate;
} 