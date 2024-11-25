package com.example.csticaret.service;

import com.example.csticaret.dto.CommentResponseDto;
import com.example.csticaret.model.Comment;
import com.example.csticaret.model.Product;
import com.example.csticaret.model.User;
import com.example.csticaret.repository.CommentRepository;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long productId, Long userId, String content, int rating) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Comment comment = new Comment(content, rating, user, product);
        return commentRepository.save(comment);
    }

    public List<CommentResponseDto> getApprovedComments(Long productId) {
        List<Comment> comments = commentRepository.findByProductIdAndApprovedTrue(productId);

        // Comment -> CommentResponseDto dönüşümü
        return comments.stream().map(comment -> {
            CommentResponseDto dto = new CommentResponseDto();
            dto.setId(comment.getId());
            dto.setContent(comment.getContent());
            dto.setRating(comment.getRating());
            dto.setApproved(comment.isApproved());
            dto.setUserFullName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
            dto.setProductName(comment.getProduct().getName());
            return dto;
        }).collect(Collectors.toList());
    }


    public List<CommentResponseDto> getAllComments() {
        List<Comment> comments = commentRepository.findAll();

        // Comment -> CommentResponseDto Dönüşümü
        return comments.stream().map(comment -> {
            CommentResponseDto dto = new CommentResponseDto();
            dto.setId(comment.getId());
            dto.setContent(comment.getContent());
            dto.setRating(comment.getRating());
            dto.setApproved(comment.isApproved());
            dto.setUserFullName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
            dto.setProductName(comment.getProduct().getName());
            return dto;
        }).collect(Collectors.toList());
    }


    public CommentResponseDto approveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        comment.setApproved(true);
        Comment updatedComment = commentRepository.save(comment);

        // Comment -> CommentResponseDto Dönüşümü
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(updatedComment.getId());
        dto.setContent(updatedComment.getContent());
        dto.setRating(updatedComment.getRating());
        dto.setApproved(updatedComment.isApproved());
        dto.setUserFullName(updatedComment.getUser().getFirstName() + " " + updatedComment.getUser().getLastName());
        dto.setProductName(updatedComment.getProduct().getName());
        return dto;
}

}
