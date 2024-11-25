package com.example.csticaret.request;

import com.example.csticaret.model.Category;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateRequest { private Long id;
    private String name;
    private String brand;
    private BigDecimal price;
    private int inventory;
    private String description;
    private int level;
    private int duration;
    private int moduleCount;
    private boolean certification;
    private String instructorName;
    private String instructorRole;
    private String thumbnailUrl;
    private List<String> curriculum;
    private Category category;
}
