package com.example.csticaret.controller.category;

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

    // Helper Method: Validate ResponseEntity
    private void validateResponse(ResponseEntity<ApiResponse> response, int statusCode, String message, Object data) {
        assertEquals(statusCode, response.getStatusCodeValue());
        assertEquals(message, response.getBody().getMessage());
        assertEquals(data, response.getBody().getData());
    }

    // Test: Get All Categories - Success
    @Test
    void testGetAllCategories_Success() {
        Category category = new Category("Electronics");
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        ResponseEntity<ApiResponse> response = categoryController.getAllCategories();

        validateResponse(response, 200, "Found!", List.of(category));
        verify(categoryService, times(1)).getAllCategories();
    }

    // Test: Add Category - Success
    @Test
    void testAddCategory_Success() {
        Category category = new Category("Electronics");
        when(categoryService.addCategory(category)).thenReturn(category);

        ResponseEntity<ApiResponse> response = categoryController.addCategory(category);

        validateResponse(response, 200, "Success", category);
        verify(categoryService, times(1)).addCategory(category);
    }

    // Test: Add Category - Already Exists
    @Test
    void testAddCategory_AlreadyExists() {
        Category category = new Category("Electronics");
        when(categoryService.addCategory(category)).thenThrow(new AlreadyExistsException("Category 'Electronics' already exists"));

        ResponseEntity<ApiResponse> response = categoryController.addCategory(category);

        validateResponse(response, 409, "Category 'Electronics' already exists", null);
        verify(categoryService, times(1)).addCategory(category);
    }

    // Test: Get Category By ID - Success
    @Test
    void testGetCategoryById_Success() {
        Long categoryId = 1L;
        Category category = new Category("Electronics");
        category.setId(categoryId);
        when(categoryService.getCategoryById(categoryId)).thenReturn(category);

        ResponseEntity<ApiResponse> response = categoryController.getCategoryById(categoryId);

        validateResponse(response, 200, "Found", category);
        verify(categoryService, times(1)).getCategoryById(categoryId);
    }

    // Test: Get Category By ID - Not Found
    @Test
    void testGetCategoryById_NotFound() {
        Long categoryId = 1L;
        when(categoryService.getCategoryById(categoryId)).thenThrow(new ResourceNotFoundException("Category with ID " + categoryId + " not found"));

        ResponseEntity<ApiResponse> response = categoryController.getCategoryById(categoryId);

        validateResponse(response, 404, "Category with ID " + categoryId + " not found", null);
        verify(categoryService, times(1)).getCategoryById(categoryId);
    }

    // Test: Delete Category - Success
    @Test
    void testDeleteCategory_Success() {
        Long categoryId = 1L;
        doNothing().when(categoryService).deleteCategoryById(categoryId);

        ResponseEntity<ApiResponse> response = categoryController.deleteCategory(categoryId);

        validateResponse(response, 200, "Deleted successfully", null);
        verify(categoryService, times(1)).deleteCategoryById(categoryId);
    }

    // Test: Delete Category - Not Found
    @Test
    void testDeleteCategory_NotFound() {
        Long categoryId = 1L;
        doThrow(new ResourceNotFoundException("Category with ID " + categoryId + " not found")).when(categoryService).deleteCategoryById(categoryId);

        ResponseEntity<ApiResponse> response = categoryController.deleteCategory(categoryId);

        validateResponse(response, 404, "Category with ID " + categoryId + " not found", null);
        verify(categoryService, times(1)).deleteCategoryById(categoryId);
    }

    // Test: Update Category - Success
    @Test
    void testUpdateCategory_Success() {
        Long categoryId = 1L;
        Category category = new Category("Electronics");
        category.setId(categoryId);
        when(categoryService.updateCategory(category, categoryId)).thenReturn(category);

        ResponseEntity<ApiResponse> response = categoryController.updateCategory(categoryId, category);

        validateResponse(response, 200, "Update success!", category);
        verify(categoryService, times(1)).updateCategory(category, categoryId);
    }

    // Test: Update Category - Not Found
    @Test
    void testUpdateCategory_NotFound() {
        Long categoryId = 1L;
        Category category = new Category("Electronics");
        when(categoryService.updateCategory(category, categoryId)).thenThrow(new ResourceNotFoundException("Category with ID " + categoryId + " not found"));

        ResponseEntity<ApiResponse> response = categoryController.updateCategory(categoryId, category);

        validateResponse(response, 404, "Category with ID " + categoryId + " not found", null);
        verify(categoryService, times(1)).updateCategory(category, categoryId);
    }
}
