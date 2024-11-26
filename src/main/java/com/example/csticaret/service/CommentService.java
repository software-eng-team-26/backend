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
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public Comment addComment(Long productId, Long userId, String content, Integer rating) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setProduct(product);
        comment.setUser(user);
        comment.setContent(content);
        comment.setRating(rating);
        comment.setApproved(false); // Comments need approval by default

        return commentRepository.save(comment);
    }

    public List<CommentResponseDto> getApprovedComments(Long productId) {
        List<Comment> comments = commentRepository.findByProductIdAndApprovedTrue(productId);
        return comments.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    private CommentResponseDto convertToDto(Comment comment) {
        return CommentResponseDto.builder()
            .id(comment.getId())
            .userId(comment.getUser().getId())
            .userName(comment.getUser().getEmail()) // or name if you have it
            .content(comment.getContent())
            .rating(comment.getRating())
            .createdAt(comment.getCreatedAt().toString())
            .build();
    }
}
