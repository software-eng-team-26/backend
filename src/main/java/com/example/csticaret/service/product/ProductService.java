package com.example.csticaret.service.product;

import com.example.csticaret.dto.ImageDto;
import com.example.csticaret.dto.ProductDto;
import com.example.csticaret.exceptions.ResourceNotFoundException;
import com.example.csticaret.model.Category;
import com.example.csticaret.model.Image;
import com.example.csticaret.model.Product;
import com.example.csticaret.repository.CategoryRepository;
import com.example.csticaret.repository.ImageRepository;
import com.example.csticaret.repository.ProductRepository;
import com.example.csticaret.request.AddProductRequest;
import com.example.csticaret.request.ProductUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;

    @Override
    public Product addProduct(AddProductRequest request) {
        // check if the category is found in the DB
        // If Yes, set it as the new product category
        // If No, the save it as a new category
        // The set as the new product category.

        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategory().getName()))
                .orElseGet(() -> {
                    Category newCategory = new Category(request.getCategory().getName());
                    return categoryRepository.save(newCategory);
                });
        request.setCategory(category);
        return productRepository.save(createProduct(request, category));
    }

    private Product createProduct(AddProductRequest request, Category category) {
        return new Product(
                request.getName(),
                request.getBrand(),
                request.getPrice(),
                request.getInventory(),
                request.getDescription(),
                request.getLevel(),
                request.getDuration(),
                request.getModuleCount(),
                request.isCertification(),
                request.getInstructorName(),
                request.getInstructorRole(),
                request.getThumbnailUrl(),
                request.getCurriculum(),
                category
        );
    }


    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found!"));
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.findById(id)
                .ifPresentOrElse(productRepository::delete,
                        () -> {throw new ResourceNotFoundException("Product not found!");});
    }

    @Override
    public Product updateProduct(ProductUpdateRequest request, Long productId) {
        return productRepository.findById(productId)
                .map(existingProduct -> updateExistingProduct(existingProduct,request))
                .map(productRepository :: save)
                .orElseThrow(()-> new ResourceNotFoundException("Product not found!"));
    }

    private Product updateExistingProduct(Product existingProduct, ProductUpdateRequest request) {
        existingProduct.setName(request.getName());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setInventory(request.getInventory());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setLevel(request.getLevel());
        existingProduct.setDuration(request.getDuration());
        existingProduct.setModuleCount(request.getModuleCount());
       // existingProduct.isCertification(request.getC());
        existingProduct.setInstructorName(request.getInstructorName());
        existingProduct.setInstructorRole(request.getInstructorRole());
        existingProduct.setThumbnailUrl(request.getThumbnailUrl());
        existingProduct.setCurriculum(request.getCurriculum());




        Category category = categoryRepository.findByName(request.getCategory().getName());
        existingProduct.setCategory(category);
        return  existingProduct;

    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
      return products.stream().map(this::convertToDto).toList();
    }

    private ImageDto convertToImageDto(Image image) {
        ImageDto dto = new ImageDto();
        dto.setId(image.getId());
        dto.setFileName(image.getFileName()); // Map fileName field
        dto.setDownloadUrl(image.getDownloadUrl()); // Map downloadUrl field
        return dto;
    }


    @Override
    public ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getName());
        dto.setBrand(product.getBrand());
        dto.setPrice(product.getPrice());
        dto.setInventory(product.getInventory());
        dto.setDescription(product.getDescription());
        dto.setLevel(product.getLevel());
        dto.setDuration(product.getDuration());
        dto.setModuleCount(product.getModuleCount());
        dto.setCertification(product.isCertification());
        dto.setInstructor(product.getInstructorName());
        dto.setInstructorRole(product.getInstructorRole());
        dto.setThumbnail(product.getThumbnailUrl());
        dto.setCurriculum(product.getCurriculum());
        dto.setCategory(product.getCategory());
        dto.setFeatured(product.getFeatured());

        // Convert List<Image> to List<ImageDto>
        List<ImageDto> imageDtos = product.getImages()
                .stream()
                .map(this::convertToImageDto)
                .toList();
        dto.setImages(imageDtos);

        return dto;
    }

    @Override
    public List<Product> getProductsByInstructorName(String instructorName) {
        return productRepository.findByInstructorName(instructorName);

    }
}
