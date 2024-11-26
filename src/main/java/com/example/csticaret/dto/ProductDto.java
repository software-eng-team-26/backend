package com.example.csticaret.dto;

import com.example.csticaret.model.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    private Long id;
    private String title;
    private String brand;
    private BigDecimal price;
    private int inventory;
    private String description;
    private int level;
    private int duration;
    private int moduleCount;
    private boolean certification;
    private String instructor;
    private String instructorRole;
    private String thumbnail;
    private List<String> curriculum;
    private Category category;
    private List<ImageDto> images;
    private Boolean featured;
    private Boolean isNew;
}
