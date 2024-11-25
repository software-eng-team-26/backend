package com.example.csticaret.controller;

import com.example.csticaret.dto.CommentDto;
import com.example.csticaret.model.Comment;
import com.example.csticaret.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add")
    public ResponseEntity<Comment> addComment(@RequestBody CommentDto commentDTO) {
        Comment comment = commentService.addComment(
                commentDTO.getProductId(),
                commentDTO.getUserId(),
                commentDTO.getContent(),
                commentDTO.getRating()
        );
        return ResponseEntity.ok(comment);
    }



    @GetMapping("/approved/{productId}")
    public ResponseEntity<List<Comment>> getApprovedComments(@PathVariable Long productId) {
        List<Comment> comments = commentService.getApprovedComments(productId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Comment>> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<Comment> approveComment(@PathVariable Long id) {
        Comment comment = commentService.approveComment(id);
        return ResponseEntity.ok(comment);
    }
}
