package com.cs308.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "productId")
    private Long productId;

    @Column(name = "productName", length = 100, nullable = false)
    private String productName;

    @Column(name = "productDescription", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "productPrice", precision = 10, scale = 2)
    private Double productPrice;

    @Column(name = "productQuantity")
    private Integer productQuantity;

    @Column(name = "productStock")
    private Integer productStock;

    @Column(name = "productModel", length = 50)
    private String productModel;

    @Column(name = "serialNumber", length = 50)
    private String serialNumber;

    @Column(name = "productWarranty")
    private Integer productWarranty;

    @Column(name = "distributor", length = 100)
    private String distributor;

    @ManyToOne
    @JoinColumn(name = "categoryId", referencedColumnName = "categoryId")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "discountId", referencedColumnName = "discountId")
    private Discount discount;
}
