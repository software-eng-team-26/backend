package com.example.csticaret.Controller.Category;


import com.example.csticaret.exceptions.AlreadyExistsException;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.Category;
import com.example.csticaret.repository.CategoryRepository;
import com.example.csticaret.service.category.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCategoryById_Success() {
        // Arrange
        Long categoryId = 1L;
        Category mockCategory = new Category();
        mockCategory.setId(categoryId);
        mockCategory.setName("Electronics");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));

        // Act
        Category result = categoryService.getCategoryById(categoryId);

        // Assert
        assertNotNull(result);
        assertEquals(categoryId, result.getId());
        assertEquals("Electronics", result.getName());
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void testGetCategoryById_NotFound() {
        // Arrange
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(categoryId));
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    void testGetCategoryByName_Success() {
        // Arrange
        String categoryName = "Electronics";
        Category mockCategory = new Category();
        mockCategory.setId(1L);
        mockCategory.setName(categoryName);

        when(categoryRepository.findByName(categoryName)).thenReturn(mockCategory);

        // Act
        Category result = categoryService.getCategoryByName(categoryName);

        // Assert
        assertNotNull(result);
        assertEquals(categoryName, result.getName());
        verify(categoryRepository, times(1)).findByName(categoryName);
    }

    @Test
    void testGetAllCategories_Success() {
        // Arrange
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Electronics");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Books");

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        // Act
        List<Category> result = categoryService.getAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).getName());
        assertEquals("Books", result.get(1).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testAddCategory_Success() {
        // Arrange
        Category newCategory = new Category();
        newCategory.setName("Clothing");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Clothing");

        when(categoryRepository.existsByName(newCategory.getName())).thenReturn(false);
        when(categoryRepository.save(newCategory)).thenReturn(savedCategory);

        // Act
        Category result = categoryService.addCategory(newCategory);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Clothing", result.getName());
        verify(categoryRepository, times(1)).existsByName(newCategory.getName());
        verify(categoryRepository, times(1)).save(newCategory);
    }

    @Test
    void testAddCategory_AlreadyExists() {
        // Arrange
        Category newCategory = new Category();
        newCategory.setName("Electronics");

        when(categoryRepository.existsByName(newCategory.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> categoryService.addCategory(newCategory));
        verify(categoryRepository, times(1)).existsByName(newCategory.getName());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testUpdateCategory_Success() {
        // Arrange
        Long categoryId = 1L;
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Electronics");

        Category updatedCategory = new Category();
        updatedCategory.setName("Updated Electronics");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        // Act
        Category result = categoryService.updateCategory(updatedCategory, categoryId);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Electronics", result.getName());
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(existingCategory);
    }

    @Test
    void testUpdateCategory_NotFound() {
        // Arrange
        Long categoryId = 1L;
        Category updatedCategory = new Category();
        updatedCategory.setName("Updated Electronics");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.updateCategory(updatedCategory, categoryId));
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testDeleteCategoryById_Success() {
        // Arrange
        Long categoryId = 1L;
        Category mockCategory = new Category();
        mockCategory.setId(categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));

        // Act
        categoryService.deleteCategoryById(categoryId);

        // Assert
        verify(categoryRepository, times(1)).delete(mockCategory);
    }

    @Test
    void testDeleteCategoryById_NotFound() {
        // Arrange
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteCategoryById(categoryId));
        verify(categoryRepository, never()).delete(any(Category.class));
    }
}
