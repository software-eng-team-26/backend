package com.cs308.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Entity
@Table(name = "wishlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlistId")
    private Integer wishlistId;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "productId", referencedColumnName = "productId")
    private Product product;
}

