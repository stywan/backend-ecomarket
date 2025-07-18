package com.ecomarket.backend.catalog_product.controller;

import com.ecomarket.backend.catalog_product.DTO.request.ProductImageRequest;
import com.ecomarket.backend.catalog_product.DTO.request.ProductRequest;
import com.ecomarket.backend.catalog_product.DTO.response.BrandResponse;
import com.ecomarket.backend.catalog_product.DTO.response.CategoryResponse;
import com.ecomarket.backend.catalog_product.DTO.response.ProductResponse;
import com.ecomarket.backend.catalog_product.assembler.ProductAssembler;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.Product;
import com.ecomarket.backend.catalog_product.model.ProductImage;
import com.ecomarket.backend.catalog_product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductAssembler productAssembler;

    // Datos de prueba comunes
    private ProductRequest productRequest;
    private Product testProduct;
    private ProductResponse testProductResponse;
    private EntityModel<ProductResponse> testProductEntityModel;
    private ProductImageRequest productImageRequest;

    private CategoryResponse testCategoryResponse;
    private BrandResponse testBrandResponse;

    @BeforeEach
    void setUp() {
        productRequest = ProductRequest.builder()
                .name("Test Product")
                .description("A cool product.")
                .price(new BigDecimal("99.99"))
                .sku("TEST-SKU-001")
                .categoryId(1L)
                .brandId(1L)
                .weight(BigDecimal.valueOf(1.0))
                .dimensions("10x10x10")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("A cool product.")
                .price(new BigDecimal("99.99"))
                .sku("TEST-SKU-001")
                .status(Product.ProductStatus.ACTIVE)
                .creationDate(LocalDateTime.now())
                .lastUpdate(LocalDateTime.now())
                .build();

        testProductResponse = ProductResponse.builder()
                .id(1L)
                .name("Test Product")
                .description("A cool product.")
                .price(new BigDecimal("99.99"))
                .sku("TEST-SKU-001")
                .category(testCategoryResponse)
                .brand(testBrandResponse)
                .status("ACTIVE")
                .build();

        testProductEntityModel = EntityModel.of(testProductResponse,
                linkTo(methodOn(ProductController.class).getProduct(testProduct.getId())).withSelfRel());

        when(productAssembler.toModel(any(Product.class))).thenReturn(testProductEntityModel);

        productImageRequest = ProductImageRequest.builder()
                .url("http://example.com/test-image.jpg")
                .build();
    }

    // --- Tests para createProduct ---
    @Test
    @DisplayName("POST / - Should create a product and return 201 Created")
    void createProduct_success() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.name").value(testProduct.getName()))
                .andExpect(jsonPath("$.status").value(testProduct.getStatus().name()))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(productService, times(1)).createProduct(any(ProductRequest.class));
        verify(productAssembler, times(1)).toModel(testProduct);
    }

    @Test
    @DisplayName("POST / - Should return 400 Bad Request for invalid product request")
    void createProduct_invalidRequest() throws Exception {
        ProductRequest invalidRequest = ProductRequest.builder()
                .name("") // Invalid: @NotBlank or @NotEmpty validation
                .description("Description")
                .price(new BigDecimal("10.00"))
                .sku("SKU123")
                .categoryId(1L)
                .brandId(1L)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists()); // Verify validation error message

        verify(productService, never()).createProduct(any(ProductRequest.class));
        verify(productAssembler, never()).toModel(any(Product.class));
    }

    @Test
    @DisplayName("POST / - Should return 404 Not Found if category or brand not found (service throws ResourceNotFoundException)")
    void createProduct_dependencyNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Category not found")).when(productService)
                .createProduct(any(ProductRequest.class));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Category not found"));

        verify(productService, times(1)).createProduct(any(ProductRequest.class));
        verify(productAssembler, never()).toModel(any(Product.class));
    }

    // --- Tests para getProduct ---
    @Test
    @DisplayName("GET /{id} - Should return product when found")
    void getProduct_success() throws Exception {
        when(productService.getProduct(anyLong())).thenReturn(testProduct);

        mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.name").value(testProduct.getName()))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(productService, times(1)).getProduct(testProduct.getId());
        verify(productAssembler, times(1)).toModel(testProduct);
    }

    @Test
    @DisplayName("GET /{id} - Should return 404 Not Found when product not found")
    void getProduct_notFound() throws Exception {
        when(productService.getProduct(anyLong())).thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(get("/api/v1/products/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService, times(1)).getProduct(99L);
        verify(productAssembler, never()).toModel(any(Product.class));
    }

    // --- Tests para updateProduct ---
    @Test
    @DisplayName("PUT /{id} - Should update product and return 200 OK")
    void updateProduct_success() throws Exception {
        Product updatedProduct = Product.builder()
                .id(1L)
                .name("Updated Product Name")
                .description("Updated description")
                .price(new BigDecimal("150.00"))
                .sku("UPDATED-SKU")
                .status(Product.ProductStatus.ACTIVE)
                .lastUpdate(LocalDateTime.now())
                .build();
        ProductResponse updatedProductResponse = ProductResponse.builder()
                .id(1L)
                .name("Updated Product Name")
                .description("Updated description")
                .price(new BigDecimal("150.00"))
                .sku("UPDATED-SKU")
                .status("ACTIVE")
                .build();
        EntityModel<ProductResponse> updatedEntityModel = EntityModel.of(updatedProductResponse,
                linkTo(methodOn(ProductController.class).getProduct(updatedProduct.getId())).withSelfRel());

        when(productService.updateProduct(anyLong(), any(ProductRequest.class))).thenReturn(updatedProduct);
        when(productAssembler.toModel(updatedProduct)).thenReturn(updatedEntityModel);

        mockMvc.perform(put("/api/v1/products/{id}", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.name").value("Updated Product Name"))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(productService, times(1)).updateProduct(testProduct.getId(), productRequest);
        verify(productAssembler, times(1)).toModel(updatedProduct);
    }

    @Test
    @DisplayName("PUT /{id} - Should return 400 Bad Request for invalid update request")
    void updateProduct_invalidRequest() throws Exception {
        ProductRequest invalidRequest = ProductRequest.builder()
                .name("") // Invalid
                .description("Description")
                .price(new BigDecimal("10.00"))
                .sku("SKU123")
                .categoryId(1L)
                .brandId(1L)
                .build();

        mockMvc.perform(put("/api/v1/products/{id}", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());

        verify(productService, never()).updateProduct(anyLong(), any(ProductRequest.class));
        verify(productAssembler, never()).toModel(any(Product.class));
    }

    @Test
    @DisplayName("PUT /{id} - Should return 404 Not Found when product to update is not found")
    void updateProduct_notFound() throws Exception {
        when(productService.updateProduct(anyLong(), any(ProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(put("/api/v1/products/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService, times(1)).updateProduct(99L, productRequest);
        verify(productAssembler, never()).toModel(any(Product.class));
    }

    // --- Tests para deleteProduct ---
    @Test
    @DisplayName("DELETE /{id} - Should delete product (soft delete) and return 204 No Content")
    void deleteProduct_success() throws Exception {
        doNothing().when(productService).deleteProduct(anyLong());

        mockMvc.perform(delete("/api/v1/products/{id}", testProduct.getId()))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(testProduct.getId());
    }

    @Test
    @DisplayName("DELETE /{id} - Should return 404 Not Found when product to delete is not found")
    void deleteProduct_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Product not found")).when(productService).deleteProduct(anyLong());

        mockMvc.perform(delete("/api/v1/products/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService, times(1)).deleteProduct(99L);
    }

    // --- Tests para addImage ---
    @Test
    @DisplayName("POST /{id}/images - Should add image to product and return 200 OK")
    void addImage_success() throws Exception {
        Product productWithImage = Product.builder().id(1L).name("Test Product").status(Product.ProductStatus.ACTIVE).build();
        productWithImage.setImages(Collections.singletonList(ProductImage.builder().id(1L).url("http://example.com/test-image.jpg").build()));
        ProductResponse productWithImageResponse = ProductResponse.builder().id(1L).name("Test Product").status("ACTIVE").build();

        EntityModel<ProductResponse> productWithImageEntityModel = EntityModel.of(productWithImageResponse,
                linkTo(methodOn(ProductController.class).getProduct(productWithImage.getId())).withSelfRel());

        when(productService.addImage(anyLong(), any(ProductImageRequest.class))).thenReturn(productWithImage);
        when(productAssembler.toModel(productWithImage)).thenReturn(productWithImageEntityModel);


        mockMvc.perform(post("/api/v1/products/{id}/images", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productImageRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testProduct.getId()))
                .andExpect(jsonPath("$._links.self.href").exists());

        verify(productService, times(1)).addImage(testProduct.getId(), productImageRequest);
        verify(productAssembler, times(1)).toModel(productWithImage);
    }

    @Test
    @DisplayName("POST /{id}/images - Should return 400 Bad Request for invalid image request DTO")
    void addImage_invalidRequest() throws Exception {
        ProductImageRequest invalidRequest = ProductImageRequest.builder()
                .url("") // Invalid: @NotBlank for URL
                .build();

        mockMvc.perform(post("/api/v1/products/{id}/images", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());

        verify(productService, never()).addImage(anyLong(), any(ProductImageRequest.class));
        verify(productAssembler, never()).toModel(any(Product.class));
    }

    @Test
    @DisplayName("POST /{id}/images - Should return 404 Not Found when product to add image to is not found")
    void addImage_productNotFound() throws Exception {
        when(productService.addImage(anyLong(), any(ProductImageRequest.class)))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        mockMvc.perform(post("/api/v1/products/{id}/images", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productImageRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found"));

        verify(productService, times(1)).addImage(99L, productImageRequest);
        verify(productAssembler, never()).toModel(any(Product.class));
    }

    // --- Tests para searchProducts ---
    @Test
    @DisplayName("GET / - Should return products by name search")
    void searchProducts_byName() throws Exception {
        List<Product> products = Collections.singletonList(testProduct);
        List<EntityModel<ProductResponse>> productModels = products.stream()
                .map(p -> EntityModel.of(testProductResponse, linkTo(methodOn(ProductController.class).getProduct(p.getId())).withSelfRel()))
                .collect(Collectors.toList());
        CollectionModel<EntityModel<ProductResponse>> collectionModel = CollectionModel.of(productModels);

        when(productService.searchProducts(anyString(), any(), any(), any())).thenReturn(products);
        when(productAssembler.toModel(any(Product.class))).thenReturn(testProductEntityModel);

        mockMvc.perform(get("/api/v1/products")
                        .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productResponseList[0].name").value("Test Product"));

        verify(productService, times(1)).searchProducts("Test", null, null, null);
        verify(productAssembler, times(1)).toModel(testProduct);
    }

    @Test
    @DisplayName("GET / - Should return products by SKU search")
    void searchProducts_bySku() throws Exception {
        List<Product> products = Collections.singletonList(testProduct);
        when(productService.searchProducts(any(), anyString(), any(), any())).thenReturn(products);
        when(productAssembler.toModel(any(Product.class))).thenReturn(testProductEntityModel);

        mockMvc.perform(get("/api/v1/products")
                        .param("sku", "TEST-SKU-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productResponseList[0].sku").value("TEST-SKU-001"));

        verify(productService, times(1)).searchProducts(null, "TEST-SKU-001", null, null);
        verify(productAssembler, times(1)).toModel(testProduct);
    }


    @Test
    @DisplayName("GET / - Should return all products when no search parameters are provided")
    void searchProducts_noParams() throws Exception {
        Product anotherProduct = Product.builder().id(2L).name("Another Product").build();
        List<Product> products = Arrays.asList(testProduct, anotherProduct);
        ProductResponse anotherProductResponse = ProductResponse.builder().id(2L).name("Another Product").build();

        EntityModel<ProductResponse> anotherProductEntityModel = EntityModel.of(anotherProductResponse,
                linkTo(methodOn(ProductController.class).getProduct(anotherProduct.getId())).withSelfRel());


        when(productService.searchProducts(any(), any(), any(), any())).thenReturn(products);
        when(productAssembler.toModel(testProduct)).thenReturn(testProductEntityModel);
        when(productAssembler.toModel(anotherProduct)).thenReturn(anotherProductEntityModel);


        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productResponseList.length()").value(2))
                .andExpect(jsonPath("$._embedded.productResponseList[0].name").value("Test Product"))
                .andExpect(jsonPath("$._embedded.productResponseList[1].name").value("Another Product"));

        verify(productService, times(1)).searchProducts(null, null, null, null);
        verify(productAssembler, times(1)).toModel(testProduct);
        verify(productAssembler, times(1)).toModel(anotherProduct);
    }

}
