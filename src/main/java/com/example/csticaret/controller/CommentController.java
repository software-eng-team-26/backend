package com.example.csticaret.controller;

import com.example.csticaret.dto.ApiResponse;
import com.example.csticaret.dto.CommentDto;
import com.example.csticaret.dto.CommentResponseDto;
import com.example.csticaret.dto.RatingDto;
import com.example.csticaret.model.Comment;
import com.example.csticaret.model.User;
import com.example.csticaret.service.CommentService;
import com.example.csticaret.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;
    private final UserRepository userRepository;

    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/rating")
    public ResponseEntity<ApiResponse<Void>> addRating(
            @RequestBody RatingDto ratingDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
//            log.info("Received rating request: {}", ratingDto);
//            log.info("User details: {}", userDetails);

            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            log.info("Found user: {}", user.getEmail());

            commentService.addRating(
                ratingDto.getProductId(),
                user.getId(),
                ratingDto.getRating()
            );
            return ResponseEntity.ok(new ApiResponse<>("Rating added successfully", null));
        } catch (Exception e) {
            log.error("Error adding rating", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CommentResponseDto>> addComment(
            @RequestBody CommentDto commentDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Received comment request. User authenticated: {}", userDetails != null);
            if (userDetails != null) {
                log.info("Authenticated user email: {}", userDetails.getUsername());
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            log.info("Found user in database: {}", user.getEmail());
            
            Comment comment = commentService.addComment(
                commentDto.getProductId(),
                user.getId(),
                commentDto.getContent(),
                commentDto.getRating()
            );
            String message = commentDto.getRating() == null 
                ? "Comment submitted for review" 
                : "Comment submitted for review and rating added";
            
            return ResponseEntity.ok(new ApiResponse<>(message, CommentResponseDto.fromComment(comment)));
        } catch (Exception e) {
            log.error("Error adding comment", e);
            return ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage(), null));
        }
    }

    @GetMapping("/approved/{productId}")
    public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getApprovedComments(@PathVariable Long productId) {
        List<CommentResponseDto> comments = commentService.getApprovedComments(productId);
        return ResponseEntity.ok(new ApiResponse<>("Comments retrieved successfully", comments));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getAllComments() {
        List<CommentResponseDto> comments = commentService.getAllComments();
        return ResponseEntity.ok(new ApiResponse<>("All comments retrieved successfully", comments));
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommentResponseDto>> approveComment(@PathVariable Long id) {
        CommentResponseDto approvedComment = commentService.approveComment(id);
        return ResponseEntity.ok(new ApiResponse<>("Comment approved successfully", approvedComment));
    }
}