package com.example.csticaret.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentResponseDto {
    private Long id;
    private String content;
    private int rating;
    private boolean approved;
    private String userFullName;
    private String productName;
}