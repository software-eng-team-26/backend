package com.cs308.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="refund")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="refundId")
    private Integer refundId;
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "userId", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "productId", referencedColumnName = "productId", nullable = false)
    private Product product;

    @Column(name = "amount", precision = 10, scale = 2)
    private Integer amount;
}




