package com.example.csticaret.service;

import com.example.csticaret.model.Comment;
import com.example.csticaret.model.Product;
import com.example.csticaret.model.User;
import com.example.csticaret.repository.CommentRepository;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Comment> getApprovedComments(Long productId) {
        return commentRepository.findByProductIdAndApprovedTrue(productId);
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Comment approveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.setApproved(true);
        return commentRepository.save(comment);
    }
}
