package com.example.csticaret.dto;

import lombok.Data;

@Data
public class RatingDto {
    private Long productId;
    private Long userId;
    private int rating;
} 