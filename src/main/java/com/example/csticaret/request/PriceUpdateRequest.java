package com.example.csticaret.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceUpdateRequest {
    private BigDecimal price;
} 