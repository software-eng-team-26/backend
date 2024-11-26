package com.example.csticaret.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String content;
    private Integer rating;
    private LocalDateTime createdAt;
    private boolean approved;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
