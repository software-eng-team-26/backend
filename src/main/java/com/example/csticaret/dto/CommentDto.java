package com.example.csticaret.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentDto {
    private Long productId;
    private Long userId;
    private String content;
    private int rating;
}