package com.cs308.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Entity
@Table(name = "shipping")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shippingId")
    private Long shippingId;

    @Column(name = "shippingAddress", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "shippingStatus", length = 20)
    private String shippingStatus;
}

