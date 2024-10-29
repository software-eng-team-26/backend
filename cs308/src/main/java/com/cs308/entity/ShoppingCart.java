package com.cs308.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;



@Entity
@Table(name = "shoppingCart")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartId")
    private Long cartId;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "productId", referencedColumnName = "productId", nullable = false)
    private Product product;

    @Column(name = "totalPrice", precision = 10, scale = 2)
    private Integer totalPrice;
}
