package com.ecomarket.backend.catalog_product.service;

import com.ecomarket.backend.catalog_product.DTO.request.ProductImageRequest;
import com.ecomarket.backend.catalog_product.DTO.request.ProductRequest;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.*;
import com.ecomarket.backend.catalog_product.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepo;
    @Mock
    private CategoryRepository categoryRepo;
    @Mock
    private BrandRepository brandRepo;
    @Mock
    private ProductImageRepository imageRepo;
    @Mock
    private InventoryRepository inventoryRepo;

    @InjectMocks
    private ProductService productService;

    private ProductRequest productRequest;
    private Category testCategory;
    private Brand testBrand;
    private Product testProduct;
    private ProductImageRequest productImageRequest;
    private ProductImage testProductImage;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder().id(1L).name("Electronics").build();
        testBrand = Brand.builder().id(1L).name("TechBrand").build();

        productRequest = ProductRequest.builder()
                .name("Laptop Pro")
                .description("Powerful laptop for professionals")
                .price(new BigDecimal("1200.00"))
                .sku("LP-PRO-001")
                .categoryId(1L)
                .brandId(1L)
                .weight(BigDecimal.valueOf(2.5))
                .dimensions("30x20x2")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Laptop Pro")
                .description("Powerful laptop for professionals")
                .price(new BigDecimal("1200.00"))
                .sku("LP-PRO-001")
                .category(testCategory)
                .brand(testBrand)
                .weight(BigDecimal.valueOf(2.5))
                .dimensions("30x20x2")
                .status(Product.ProductStatus.ACTIVE)
                .creationDate(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .images(new ArrayList<>()) // Inicializar la lista de imágenes
                .build();

        productImageRequest = ProductImageRequest.builder()
                .url("https://example.com/image.jpg")
                .build();

        testProductImage = ProductImage.builder()
                .id(1L)
                .product(testProduct)
                .url("https://example.com/image.jpg")
                .build();
    }

    // --- Tests para createProduct ---
    @Test
    @DisplayName("createProduct - Should create a product and its initial inventory successfully")
    void createProduct_success() {
        when(categoryRepo.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(brandRepo.findById(anyLong())).thenReturn(Optional.of(testBrand));
        when(productRepo.save(any(Product.class))).thenReturn(testProduct); // Return the product with ID after save
        when(inventoryRepo.save(any(Inventory.class))).thenAnswer(i -> i.getArguments()[0]); // Return the saved inventory

        Product createdProduct = productService.createProduct(productRequest);

        assertNotNull(createdProduct);
        assertEquals(testProduct.getId(), createdProduct.getId());
        assertEquals(Product.ProductStatus.ACTIVE, createdProduct.getStatus());
        assertEquals(testProduct.getName(), createdProduct.getName());

        verify(categoryRepo, times(1)).findById(productRequest.getCategoryId());
        verify(brandRepo, times(1)).findById(productRequest.getBrandId());
        verify(productRepo, times(1)).save(any(Product.class));

        // Captura el argumento pasado a inventoryRepo.save
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepo, times(1)).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertNotNull(savedInventory);
        assertEquals(savedInventory.getProduct().getId(), testProduct.getId());
        assertEquals(0, savedInventory.getAvailableQuantity());
        assertEquals("Default Location", savedInventory.getLocation());
        assertNotNull(savedInventory.getLastUpdate());
    }

    @Test
    @DisplayName("createProduct - Should throw ResourceNotFoundException if category not found")
    void createProduct_categoryNotFound() {
        when(categoryRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productRequest);
        });

        assertEquals("Category not found", thrown.getMessage());
        verify(categoryRepo, times(1)).findById(productRequest.getCategoryId());
        verify(brandRepo, never()).findById(anyLong()); // Should not call brandRepo
        verify(productRepo, never()).save(any(Product.class)); // Should not save product
        verify(inventoryRepo, never()).save(any(Inventory.class)); // Should not save inventory
    }

    @Test
    @DisplayName("createProduct - Should throw ResourceNotFoundException if brand not found")
    void createProduct_brandNotFound() {
        when(categoryRepo.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(brandRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(productRequest);
        });

        assertEquals("Brand not found", thrown.getMessage());
        verify(categoryRepo, times(1)).findById(productRequest.getCategoryId());
        verify(brandRepo, times(1)).findById(productRequest.getBrandId());
        verify(productRepo, never()).save(any(Product.class));
        verify(inventoryRepo, never()).save(any(Inventory.class));
    }



    @Test
    @DisplayName("updateProduct - Should throw ResourceNotFoundException if product not found")
    void updateProduct_productNotFound() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(99L, productRequest);
        });

        assertEquals("Product not found", thrown.getMessage());
        verify(productRepo, times(1)).findById(99L);
        verify(categoryRepo, never()).findById(anyLong());
        verify(brandRepo, never()).findById(anyLong());
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct - Should throw ResourceNotFoundException if category not found")
    void updateProduct_categoryNotFound() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(categoryRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(testProduct.getId(), productRequest);
        });

        assertEquals("Category not found", thrown.getMessage());
        verify(productRepo, times(1)).findById(testProduct.getId());
        verify(categoryRepo, times(1)).findById(productRequest.getCategoryId());
        verify(brandRepo, never()).findById(anyLong());
        verify(productRepo, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct - Should throw ResourceNotFoundException if brand not found")
    void updateProduct_brandNotFound() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(categoryRepo.findById(anyLong())).thenReturn(Optional.of(testCategory));
        when(brandRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(testProduct.getId(), productRequest);
        });

        assertEquals("Brand not found", thrown.getMessage());
        verify(productRepo, times(1)).findById(testProduct.getId());
        verify(categoryRepo, times(1)).findById(productRequest.getCategoryId());
        verify(brandRepo, times(1)).findById(productRequest.getBrandId());
        verify(productRepo, never()).save(any(Product.class));
    }

    // --- Tests para deleteProduct ---
    @Test
    @DisplayName("deleteProduct - Should change product status to INACTIVE successfully")
    void deleteProduct_success() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(productRepo.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        productService.deleteProduct(testProduct.getId());

        verify(productRepo, times(1)).findById(testProduct.getId());
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepo, times(1)).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals(Product.ProductStatus.INACTIVE, savedProduct.getStatus());
        assertNotNull(savedProduct.getLastUpdate()); // lastUpdate should be updated
    }

    @Test
    @DisplayName("deleteProduct - Should throw ResourceNotFoundException if product not found")
    void deleteProduct_notFound() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(99L);
        });

        assertEquals("Product not found", thrown.getMessage());
        verify(productRepo, times(1)).findById(99L);
        verify(productRepo, never()).save(any(Product.class));
    }

    // --- Tests para addImage ---
    @Test
    @DisplayName("addImage - Should add an image to product successfully")
    void addImage_success() {
        // Asegúrate de que la lista de imágenes en testProduct no sea nula
        if (testProduct.getImages() == null) {
            testProduct.setImages(new ArrayList<>());
        }

        when(productRepo.findById(anyLong())).thenReturn(Optional.of(testProduct));
        when(imageRepo.save(any(ProductImage.class))).thenAnswer(i -> {
            ProductImage savedImage = i.getArgument(0);
            savedImage.setId(1L); // Simular que JPA asigna un ID
            return savedImage;
        });
        when(productRepo.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        Product result = productService.addImage(testProduct.getId(), productImageRequest);

        assertNotNull(result);
        assertFalse(result.getImages().isEmpty());
        assertEquals(1, result.getImages().size());
        assertEquals(productImageRequest.getUrl(), result.getImages().get(0).getUrl());
        assertEquals(testProduct.getId(), result.getImages().get(0).getProduct().getId());

        verify(productRepo, times(1)).findById(testProduct.getId());
        verify(imageRepo, times(1)).save(any(ProductImage.class));
        verify(productRepo, times(1)).save(any(Product.class)); // El producto debe guardarse para persistir la relación
    }

    @Test
    @DisplayName("addImage - Should throw ResourceNotFoundException if product not found")
    void addImage_productNotFound() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            productService.addImage(99L, productImageRequest);
        });

        assertEquals("Product not found", thrown.getMessage());
        verify(productRepo, times(1)).findById(99L);
        verify(imageRepo, never()).save(any(ProductImage.class));
        verify(productRepo, never()).save(any(Product.class));
    }

    // --- Tests para removeImage ---
    @Test
    @DisplayName("removeImage - Should remove an image successfully")
    void removeImage_success() {
        when(imageRepo.findById(anyLong())).thenReturn(Optional.of(testProductImage));
        doNothing().when(imageRepo).delete(any(ProductImage.class));

        productService.removeImage(testProductImage.getId());

        verify(imageRepo, times(1)).findById(testProductImage.getId());
        verify(imageRepo, times(1)).delete(testProductImage);
    }

    @Test
    @DisplayName("removeImage - Should throw ResourceNotFoundException if image not found")
    void removeImage_notFound() {
        when(imageRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            productService.removeImage(99L);
        });

        assertEquals("Image not found", thrown.getMessage());
        verify(imageRepo, times(1)).findById(99L);
        verify(imageRepo, never()).delete(any(ProductImage.class));
    }

    // --- Tests para getProduct ---
    @Test
    @DisplayName("getProduct - Should return a product when found")
    void getProduct_success() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.of(testProduct));

        Product foundProduct = productService.getProduct(testProduct.getId());

        assertNotNull(foundProduct);
        assertEquals(testProduct.getId(), foundProduct.getId());
        verify(productRepo, times(1)).findById(testProduct.getId());
    }

    @Test
    @DisplayName("getProduct - Should throw ResourceNotFoundException when product not found")
    void getProduct_notFound() {
        when(productRepo.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProduct(99L);
        });

        assertEquals("Product not found", thrown.getMessage());
        verify(productRepo, times(1)).findById(99L);
    }

    // --- Tests para searchProducts ---
    @Test
    @DisplayName("searchProducts - Should return products by name containing")
    void searchProducts_byName() {
        when(productRepo.findByNameContaining(anyString())).thenReturn(Collections.singletonList(testProduct));

        List<Product> results = productService.searchProducts("Laptop", null, null, null);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testProduct.getName(), results.get(0).getName());
        verify(productRepo, times(1)).findByNameContaining("Laptop");
        verify(productRepo, never()).findBySku(anyString());
        verify(productRepo, never()).findByCategory_Id(anyLong());
        verify(productRepo, never()).findByBrand_Id(anyLong());
        verify(productRepo, never()).findAll();
    }

    @Test
    @DisplayName("searchProducts - Should return products by SKU")
    void searchProducts_bySku() {
        when(productRepo.findBySku(anyString())).thenReturn(Optional.of(testProduct));

        List<Product> results = productService.searchProducts(null, "LP-PRO-001", null, null);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testProduct.getSku(), results.get(0).getSku());
        verify(productRepo, times(1)).findBySku("LP-PRO-001");
        verify(productRepo, never()).findByNameContaining(anyString());
        verify(productRepo, never()).findByCategory_Id(anyLong());
        verify(productRepo, never()).findByBrand_Id(anyLong());
        verify(productRepo, never()).findAll();
    }

    @Test
    @DisplayName("searchProducts - Should return empty list when SKU not found")
    void searchProducts_bySku_notFound() {
        when(productRepo.findBySku(anyString())).thenReturn(Optional.empty());

        List<Product> results = productService.searchProducts(null, "NON-EXISTENT-SKU", null, null);

        assertTrue(results.isEmpty());
        verify(productRepo, times(1)).findBySku("NON-EXISTENT-SKU");
        verify(productRepo, never()).findByNameContaining(anyString());
    }

    @Test
    @DisplayName("searchProducts - Should return products by category ID")
    void searchProducts_byCategoryId() {
        when(productRepo.findByCategory_Id(anyLong())).thenReturn(Collections.singletonList(testProduct));

        List<Product> results = productService.searchProducts(null, null, 1L, null);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testCategory.getId(), results.get(0).getCategory().getId());
        verify(productRepo, times(1)).findByCategory_Id(1L);
        verify(productRepo, never()).findByNameContaining(anyString());
        verify(productRepo, never()).findBySku(anyString());
        verify(productRepo, never()).findByBrand_Id(anyLong());
        verify(productRepo, never()).findAll();
    }

    @Test
    @DisplayName("searchProducts - Should return products by brand ID")
    void searchProducts_byBrandId() {
        when(productRepo.findByBrand_Id(anyLong())).thenReturn(Collections.singletonList(testProduct));

        List<Product> results = productService.searchProducts(null, null, null, 1L);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(testBrand.getId(), results.get(0).getBrand().getId());
        verify(productRepo, times(1)).findByBrand_Id(1L);
        verify(productRepo, never()).findByNameContaining(anyString());
        verify(productRepo, never()).findBySku(anyString());
        verify(productRepo, never()).findByCategory_Id(anyLong());
        verify(productRepo, never()).findAll();
    }

    @Test
    @DisplayName("searchProducts - Should return all products when no filters provided")
    void searchProducts_noFilters() {
        Product anotherProduct = Product.builder().id(2L).name("Desktop PC").build();
        when(productRepo.findAll()).thenReturn(Arrays.asList(testProduct, anotherProduct));

        List<Product> results = productService.searchProducts(null, null, null, null);

        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
        verify(productRepo, times(1)).findAll();
        verify(productRepo, never()).findByNameContaining(anyString());
        verify(productRepo, never()).findBySku(anyString());
        verify(productRepo, never()).findByCategory_Id(anyLong());
        verify(productRepo, never()).findByBrand_Id(anyLong());
    }

    @Test
    @DisplayName("searchProducts - Should return empty list when no filters and no products")
    void searchProducts_noFilters_noProducts() {
        when(productRepo.findAll()).thenReturn(Collections.emptyList());

        List<Product> results = productService.searchProducts(null, null, null, null);

        assertTrue(results.isEmpty());
        verify(productRepo, times(1)).findAll();
    }
}
