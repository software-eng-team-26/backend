package com.example.csticaret.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiscountRequest {
    private Long productId;
    private Double discountRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
} 