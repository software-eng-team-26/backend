package com.example.csticaret.service.discount;

import com.example.csticaret.model.Discount;
import com.example.csticaret.model.Product;
import com.example.csticaret.repository.DiscountRepository;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountService {
    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Discount createDiscount(Long productId, Double discountRate, LocalDateTime startDate, LocalDateTime endDate) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Deactivate any existing active discounts for this product
        discountRepository.findByProductIdAndIsActiveTrue(productId)
            .ifPresent(existingDiscount -> existingDiscount.setActive(false));

        Discount discount = new Discount();
        discount.setProduct(product);
        discount.setDiscountRate(discountRate);
        discount.setStartDate(startDate);
        discount.setEndDate(endDate);
        discount.setActive(true);

        // Update product price
        BigDecimal originalPrice = product.getPrice();
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
            BigDecimal.valueOf(discountRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        BigDecimal discountedPrice = originalPrice.multiply(discountMultiplier)
            .setScale(2, RoundingMode.HALF_UP);

        product.setPrice(discountedPrice);
        product.setIsOnSale(true);
        product.setDiscountRate(discountRate);
        product.setOriginalPrice(originalPrice);
        productRepository.save(product);

        return discountRepository.save(discount);
    }

    public List<Discount> getAllDiscounts() {
        return discountRepository.findAll();
    }

    public List<Discount> getActiveDiscounts() {
        return discountRepository.findByIsActiveTrue();
    }

    @Transactional
    public void deactivateDiscount(Long discountId) {
        Discount discount = discountRepository.findById(discountId)
            .orElseThrow(() -> new ResourceNotFoundException("Discount not found"));

        discount.setActive(false);
        Product product = discount.getProduct();
        
        if (product != null) {
            if (product.getOriginalPrice() != null) {
                product.setPrice(product.getOriginalPrice());
            }
            product.setIsOnSale(false);
            product.setDiscountRate(null);
            product.setOriginalPrice(null);
            productRepository.save(product);
        }
        
        discountRepository.save(discount);
    }
} 