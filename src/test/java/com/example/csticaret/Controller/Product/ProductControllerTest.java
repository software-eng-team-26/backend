package com.example.csticaret.Controller.Product;

import com.example.csticaret.controller.ProductController;
import com.example.csticaret.dto.ProductDto;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.Product;
import com.example.csticaret.request.AddProductRequest;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.service.product.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private IProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProducts_Success() {
        // Arrange
        ProductDto mockProductDto = new ProductDto();
        mockProductDto.setId(1L);
        mockProductDto.setTitle("Test Product");
        when(productService.getConvertedProducts(anyList())).thenReturn(List.of(mockProductDto));

        // Act
        ResponseEntity<ApiResponse> response = productController.getAllProducts();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("success", response.getBody().getMessage());
        verify(productService, times(1)).getAllProducts();
        verify(productService, times(1)).getConvertedProducts(anyList());
    }

    @Test
    void testGetProductById_Success() {
        // Arrange
        Long productId = 1L;
        Product mockProduct = new Product();
        mockProduct.setId(productId);
        mockProduct.setName("Test Product");

        ProductDto mockProductDto = new ProductDto();
        mockProductDto.setId(productId);
        mockProductDto.setTitle("Test Product");

        when(productService.getProductById(productId)).thenReturn(mockProduct);
        when(productService.convertToDto(mockProduct)).thenReturn(mockProductDto);

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductById(productId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("success", response.getBody().getMessage());
        assertEquals(mockProductDto, response.getBody().getData());
        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void testGetProductById_NotFound() {
        // Arrange
        Long productId = 1L;
        when(productService.getProductById(productId)).thenThrow(new ResourceNotFoundException("Product not found"));

        // Act
        ResponseEntity<ApiResponse> response = productController.getProductById(productId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Product not found", response.getBody().getMessage());
        verify(productService, times(1)).getProductById(productId);
    }

    @Test
    void testAddProduct_Success() {
        // Arrange
        AddProductRequest request = new AddProductRequest();
        request.setName("New Product");

        Product mockProduct = new Product();
        mockProduct.setName("New Product");

        ProductDto mockProductDto = new ProductDto();
        mockProductDto.setTitle("New Product");

        when(productService.addProduct(request)).thenReturn(mockProduct);
        when(productService.convertToDto(mockProduct)).thenReturn(mockProductDto);

        // Act
        ResponseEntity<ApiResponse> response = productController.addProduct(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Add product success!", response.getBody().getMessage());
        assertEquals(mockProductDto, response.getBody().getData());
        verify(productService, times(1)).addProduct(request);
    }
}