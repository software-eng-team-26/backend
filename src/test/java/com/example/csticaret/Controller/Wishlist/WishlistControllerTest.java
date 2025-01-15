package com.example.csticaret.Controller.Wishlist;



import com.example.csticaret.controller.WishlistController;
import com.example.csticaret.model.Product;
import com.example.csticaret.model.User;
import com.example.csticaret.model.Wishlist;
import com.example.csticaret.service.wishlist.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class WishlistControllerTest {

    @InjectMocks
    private WishlistController wishlistController;

    @Mock
    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetWishlist_Success() {
        // Arrange
        Long userId = 1L;
        Wishlist mockWishlist = new Wishlist();
        mockWishlist.setId(1L);
        mockWishlist.setUser(new User());
        mockWishlist.setProducts(new HashSet<>());

        when(wishlistService.getWishlistByUserId(userId)).thenReturn(mockWishlist);

        // Act
        ResponseEntity<Wishlist> response = wishlistController.getWishlist(userId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockWishlist, response.getBody());
        verify(wishlistService, times(1)).getWishlistByUserId(userId);
    }

    @Test
    void testGetWishlist_NotFound() {
        // Arrange
        Long userId = 1L;
        when(wishlistService.getWishlistByUserId(userId)).thenReturn(null);

        // Act
        ResponseEntity<Wishlist> response = wishlistController.getWishlist(userId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        verify(wishlistService, times(1)).getWishlistByUserId(userId);
    }

    @Test
    void testAddProductToWishlist_Success() {
        // Arrange
        Long userId = 1L;
        Long productId = 2L;

        Wishlist mockWishlist = new Wishlist();
        mockWishlist.setId(1L);
        mockWishlist.setUser(new User());
        mockWishlist.setProducts(new HashSet<>());

        Product mockProduct = new Product();
        mockProduct.setId(productId);

        mockWishlist.getProducts().add(mockProduct);

        when(wishlistService.addProductToWishlist(userId, productId)).thenReturn(mockWishlist);

        // Act
        ResponseEntity<Wishlist> response = wishlistController.addProductToWishlist(userId, productId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockWishlist, response.getBody());
        assertEquals(1, response.getBody().getProducts().size());
        verify(wishlistService, times(1)).addProductToWishlist(userId, productId);
    }

    @Test
    void testRemoveProductFromWishlist_Success() {
        // Arrange
        Long userId = 1L;
        Long productId = 2L;

        Wishlist mockWishlist = new Wishlist();
        mockWishlist.setId(1L);
        mockWishlist.setUser(new User());

        Product mockProduct = new Product();
        mockProduct.setId(productId);

        Set<Product> products = new HashSet<>();
        products.add(mockProduct);
        mockWishlist.setProducts(products);

        when(wishlistService.removeProductFromWishlist(userId, productId)).thenReturn(mockWishlist);

        // Act
        ResponseEntity<Wishlist> response = wishlistController.removeProductFromWishlist(userId, productId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(mockWishlist, response.getBody());
        verify(wishlistService, times(1)).removeProductFromWishlist(userId, productId);
    }
}

