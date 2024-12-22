package com.example.csticaret.dto;

import lombok.Data;

@Data
public class CommentDto {
    private Long productId;
    private Long userId;
    private String content;
    private Integer rating;
}