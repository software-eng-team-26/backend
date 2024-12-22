package com.example.csticaret.dto;

import com.example.csticaret.model.Comment;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;
    private Integer rating;
    private boolean approved;
    private String userFullName;
    private String productName;
    private String createdAt;

    public static CommentResponseDto fromComment(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setRating(comment.getRating());
        dto.setApproved(comment.isApproved());
        dto.setUserFullName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
        dto.setProductName(comment.getProduct().getName());
        dto.setCreatedAt(comment.getCreatedAt().toString());
        return dto;
    }
}