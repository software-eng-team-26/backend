package com.example.csticaret.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.csticaret.exception.InsufficientStockException;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private Integer inventory;
    private String description;
    private int level;
    private int duration;
    private int moduleCount;
    private boolean certification;
    private String instructorName;
    private String instructorRole;
    private String thumbnailUrl;

    @ElementCollection
    private List<String> curriculum;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;

    @Column(nullable = false)
    private Boolean featured = false;

    @Column(nullable = false)
    private Double averageRating = 0.0;

    public Product(String name, String brand, BigDecimal price, int inventory, String description,
                   int level, int duration, int moduleCount, boolean certification, String instructorName,
                   String instructorRole, String thumbnailUrl,List<String> curriculum, Category category)
    {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.inventory = inventory;
        this.description = description;
        this.level = level;
        this.duration = duration;
        this.moduleCount = moduleCount;
        this.certification = certification;
        this.instructorName = instructorName;
        this.instructorRole = instructorRole;
        this.thumbnailUrl = thumbnailUrl;
        this.curriculum = curriculum;
        this.category = category;
        this.featured = false;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public boolean hasStock(int quantity) {
        return inventory >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new InsufficientStockException("Not enough stock available");
        }
        this.inventory -= quantity;
    }
}
