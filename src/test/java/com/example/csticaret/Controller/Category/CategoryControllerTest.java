package com.example.csticaret.Controller.Category;


import com.example.csticaret.controller.CategoryController;
import com.example.csticaret.exceptions.AlreadyExistsException;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.Category;
import com.example.csticaret.response.ApiResponse;
import com.example.csticaret.service.category.ICategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private ICategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllCategories_Success() {
        // Arrange
        Category category = new Category("Electronics");
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        // Act
        ResponseEntity<ApiResponse> response = categoryController.getAllCategories();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Found!", response.getBody().getMessage());
        assertEquals(1, ((List<?>) response.getBody().getData()).size());
        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testAddCategory_Success() {
        // Arrange
        Category category = new Category("Electronics");
        when(categoryService.addCategory(category)).thenReturn(category);

        // Act
        ResponseEntity<ApiResponse> response = categoryController.addCategory(category);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Success", response.getBody().getMessage());
        assertEquals(category, response.getBody().getData());
        verify(categoryService, times(1)).addCategory(category);
    }

    @Test
    void testAddCategory_AlreadyExists() {
        // Arrange
        Category category = new Category("Electronics");
        when(categoryService.addCategory(category)).thenThrow(new AlreadyExistsException("Electronics already exists"));

        // Act
        ResponseEntity<ApiResponse> response = categoryController.addCategory(category);

        // Assert
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Electronics already exists", response.getBody().getMessage());
        verify(categoryService, times(1)).addCategory(category);
    }

    @Test
    void testGetCategoryById_Success() {
        // Arrange
        Long categoryId = 1L;
        Category category = new Category("Electronics");
        category.setId(categoryId);
        when(categoryService.getCategoryById(categoryId)).thenReturn(category);

        // Act
        ResponseEntity<ApiResponse> response = categoryController.getCategoryById(categoryId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Found", response.getBody().getMessage());
        assertEquals(category, response.getBody().getData());
        verify(categoryService, times(1)).getCategoryById(categoryId);
    }

    @Test
    void testGetCategoryById_NotFound() {
        // Arrange
        Long categoryId = 1L;
        when(categoryService.getCategoryById(categoryId)).thenThrow(new ResourceNotFoundException("Category not found!"));

        // Act
        ResponseEntity<ApiResponse> response = categoryController.getCategoryById(categoryId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Category not found!", response.getBody().getMessage());
        verify(categoryService, times(1)).getCategoryById(categoryId);
    }

    @Test
    void testDeleteCategory_Success() {
        // Arrange
        Long categoryId = 1L;
        doNothing().when(categoryService).deleteCategoryById(categoryId);

        // Act
        ResponseEntity<ApiResponse> response = categoryController.deleteCategory(categoryId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Found", response.getBody().getMessage());
        verify(categoryService, times(1)).deleteCategoryById(categoryId);
    }

    @Test
    void testDeleteCategory_NotFound() {
        // Arrange
        Long categoryId = 1L;
        doThrow(new ResourceNotFoundException("Category not found!")).when(categoryService).deleteCategoryById(categoryId);

        // Act
        ResponseEntity<ApiResponse> response = categoryController.deleteCategory(categoryId);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Category not found!", response.getBody().getMessage());
        verify(categoryService, times(1)).deleteCategoryById(categoryId);
    }

    @Test
    void testUpdateCategory_Success() {
        // Arrange
        Long categoryId = 1L;
        Category category = new Category("Electronics");
        category.setId(categoryId);
        when(categoryService.updateCategory(category, categoryId)).thenReturn(category);

        // Act
        ResponseEntity<ApiResponse> response = categoryController.updateCategory(categoryId, category);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Update success!", response.getBody().getMessage());
        assertEquals(category, response.getBody().getData());
        verify(categoryService, times(1)).updateCategory(category, categoryId);
    }

    @Test
    void testUpdateCategory_NotFound() {
        // Arrange
        Long categoryId = 1L;
        Category category = new Category("Electronics");
        when(categoryService.updateCategory(category, categoryId)).thenThrow(new ResourceNotFoundException("Category not found!"));

        // Act
        ResponseEntity<ApiResponse> response = categoryController.updateCategory(categoryId, category);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Category not found!", response.getBody().getMessage());
        verify(categoryService, times(1)).updateCategory(category, categoryId);
    }
}
