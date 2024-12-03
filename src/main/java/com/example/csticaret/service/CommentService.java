package com.example.csticaret.service;

import com.example.csticaret.dto.CommentResponseDto;
import com.example.csticaret.model.Comment;
import com.example.csticaret.model.Product;
import com.example.csticaret.model.User;
import com.example.csticaret.enums.OrderStatus;
import com.example.csticaret.repository.CommentRepository;
import com.example.csticaret.repository.OrderRepository;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CommentService(CommentRepository commentRepository, 
                         OrderRepository orderRepository,
                         UserRepository userRepository,
                         ProductRepository productRepository) {
        this.commentRepository = commentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public void addRating(Long productId, Long userId, int rating) {
        boolean hasOrdered = orderRepository.existsByUserIdAndProductIdAndStatus(
            userId, 
            productId, 
            OrderStatus.DELIVERED
        );

        if (!hasOrdered) {
            throw new IllegalArgumentException("You need to purchase and receive this product before rating");
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Create a comment object for the rating without content
        Comment ratingComment = new Comment();
        ratingComment.setProduct(product);
        ratingComment.setUser(userRepository.getReferenceById(userId));
        ratingComment.setRating(rating);
        ratingComment.setContent(null);  // Explicitly set content to null for ratings
        ratingComment.setApproved(true);  // Ratings are automatically approved
        ratingComment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(ratingComment);

        // Update product's average rating
        updateProductRating(product);
    }

    public Comment addComment(Long productId, Long userId, String content, Integer rating) {
        boolean hasOrdered = orderRepository.existsByUserIdAndProductIdAndStatus(
            userId, 
            productId, 
            OrderStatus.DELIVERED
        );

        if (!hasOrdered) {
            throw new IllegalArgumentException("You need to purchase and receive this product before commenting");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Create comment (needs approval)
        Comment comment = new Comment();
        comment.setProduct(product);
        comment.setUser(user);
        comment.setContent(content);
        comment.setRating(rating);  // Set the rating on the comment itself
        comment.setApproved(false);  // Comments need approval
        comment.setCreatedAt(LocalDateTime.now());

        // If rating is included with comment
        if (rating != null) {
            // Create separate rating entry that's immediately approved
            Comment ratingComment = new Comment();
            ratingComment.setProduct(product);
            ratingComment.setUser(user);
            ratingComment.setRating(rating);
            ratingComment.setContent(null);  // No content for rating-only entries
            ratingComment.setApproved(true);  // Ratings are automatically approved
            ratingComment.setCreatedAt(LocalDateTime.now());
            commentRepository.save(ratingComment);
            
            // Update product's average rating
            updateProductRating(product);
        }

        return commentRepository.save(comment);
    }

    private void updateProductRating(Product product) {
        // Get all approved ratings
        List<Integer> approvedRatings = commentRepository.findApprovedRatingsByProductId(product.getId())
            .stream()
            .map(Comment::getRating)
            .filter(rating -> rating != null)  // Filter out null ratings
            .collect(Collectors.toList());
        
        log.debug("Calculating average rating for product {}. Approved ratings: {}", product.getId(), approvedRatings);
        
        if (approvedRatings.isEmpty()) {
            product.setAverageRating(0.0);
            productRepository.save(product);  // Save the updated product
        } else {
            // Calculate average using Integer objects
            double averageRating = approvedRatings.stream()
                .mapToDouble(Integer::doubleValue)  // Convert to double to avoid null issues
                .average()
                .orElse(0.0);

            log.debug("New average rating for product {}: {}", product.getId(), averageRating);
            product.setAverageRating(averageRating);
            productRepository.save(product);  // Save the updated product
        }
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