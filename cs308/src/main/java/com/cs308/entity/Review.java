package com.cs308.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Entity
@Table(name = "review")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reviewId")
    private Integer reviewId;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "adminApproval")
    private Boolean adminApproval;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "productId", referencedColumnName = "productId")
    private Product product;
}

