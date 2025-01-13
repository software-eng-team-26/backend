package com.example.csticaret.service.notification;

import com.example.csticaret.model.Product;
import com.example.csticaret.model.Wishlist;
import com.example.csticaret.repository.WishlistRepository;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final EmailService emailService;
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    public void notifyUsersAboutDiscount(Product product, Double discountRate) {
        try {
            log.info("Initial product ID: {}", product.getId());
            
            // Use the new method to fetch product with all details
            Product freshProduct = productRepository.findByIdWithDetails(product.getId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + product.getId()));
            
            // Debug logging
            log.info("Product found in database:");
            log.info("ID: {}", freshProduct.getId());
            log.info("Name: {}", freshProduct.getName());
            log.info("Price: {}", freshProduct.getPrice());
            log.info("Original Price: {}", freshProduct.getOriginalPrice());
            log.info("Discount Rate: {}", freshProduct.getDiscountRate());
            log.info("Is On Sale: {}", freshProduct.getIsOnSale());
            
            if (freshProduct.getName() == null || freshProduct.getName().trim().isEmpty()) {
                log.error("Product name is null or empty for product ID: {}", freshProduct.getId());
                throw new RuntimeException("Product name cannot be null or empty");
            }
            
            log.info("Fresh product details - ID: {}, Name: {}, Price: {}, Original Price: {}, Discount Rate: {}", 
                freshProduct.getId(), 
                freshProduct.getName(), 
                freshProduct.getPrice(), 
                freshProduct.getOriginalPrice(),
                discountRate
            );
            
            // Find all wishlists that contain the product
            List<Wishlist> wishlists = wishlistRepository.findByProductsContaining(freshProduct);
            log.info("Found {} wishlists containing the product", wishlists.size());
            
            for (Wishlist wishlist : wishlists) {
                String subject = "Price Drop Alert for " + freshProduct.getName();
                String emailContent = String.format("""
                    <html>
                    <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                        <h2 style="color: #2c3e50;">Price Drop Alert!</h2>
                        <p>A product from your wishlist is now on sale!</p>
                        <div style="margin: 20px 0; padding: 20px; border: 1px solid #e1e1e1; border-radius: 5px;">
                            <h3 style="color: #2c3e50; margin-bottom: 15px;">%s</h3>
                            <p style="color: #e41e31; font-size: 18px; font-weight: bold;">%.1f%% OFF</p>
                            <p style="text-decoration: line-through; color: #666;">Original Price: $%.2f</p>
                            <p style="font-size: 20px; color: #27ae60; font-weight: bold;">New Price: $%.2f</p>
                        </div>
                        <p style="color: #e67e22; font-weight: bold;">Don't miss out on this special offer!</p>
                        <br>
                        <p>Best regards,</p>
                        <p style="font-weight: bold;">EduMart Team</p>
                    </body>
                    </html>
                    """,
                    freshProduct.getName(),
                    discountRate,
                    freshProduct.getOriginalPrice().doubleValue(),
                    freshProduct.getPrice().doubleValue()
                );

                try {
                    emailService.sendEmail(wishlist.getUser().getEmail(), subject, emailContent);
                    log.info("Discount notification sent to user: {} for product: {}", 
                        wishlist.getUser().getEmail(), freshProduct.getName());
                } catch (Exception e) {
                    log.error("Failed to send discount notification to user: {} for product: {}", 
                        wishlist.getUser().getEmail(), freshProduct.getName(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error in notifyUsersAboutDiscount", e);
            throw e;
        }
    }
} 