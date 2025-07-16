package com.ecomarket.backend.catalog_product.controller;

import com.ecomarket.backend.catalog_product.DTO.ProductImageRequest;
import com.ecomarket.backend.catalog_product.DTO.ProductRequest;
import com.ecomarket.backend.catalog_product.DTO.ProductResponse;
import com.ecomarket.backend.catalog_product.assembler.ProductAssembler;
import com.ecomarket.backend.catalog_product.exception.ResourceNotFoundException;
import com.ecomarket.backend.catalog_product.model.Product;
import com.ecomarket.backend.catalog_product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión del catálogo de productos")
public class ProductController {

    private final ProductService productService;
    private final ProductAssembler productAssembler;

    @PostMapping
    @Operation(summary = "Crear un nuevo producto", description = "Crea un nuevo producto en el catálogo")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<EntityModel<ProductResponse>> createProduct(
            @Parameter(description = "Datos del nuevo producto", required = true)
            @Valid @RequestBody ProductRequest request) {
        Product product = productService.createProduct(request);
        return ResponseEntity.status(201).body(productAssembler.toModel(product));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID", description = "Obtiene un producto por su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<ProductResponse>> getProduct(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id) {
        try {
            Product product = productService.getProduct(id);
            return ResponseEntity.ok(productAssembler.toModel(product));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto", description = "Actualiza un producto existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<ProductResponse>> updateProduct(
            @Parameter(description = "ID del producto a actualizar", required = true) @PathVariable Long id,
            @Parameter(description = "Datos actualizados del producto", required = true)
            @Valid @RequestBody ProductRequest request) {
        try {
            Product product = productService.updateProduct(id, request);
            return ResponseEntity.ok(productAssembler.toModel(product));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto", description = "Elimina un producto por su ID (soft delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Producto eliminado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID del producto a eliminar", required = true) @PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/images")
    @Operation(summary = "Agregar imagen a producto", description = "Agrega una imagen al producto especificado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Imagen agregada y producto actualizado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<EntityModel<ProductResponse>> addImage(
            @Parameter(description = "ID del producto", required = true) @PathVariable Long id,
            @Parameter(description = "Datos de la imagen a agregar", required = true)
            @Valid @RequestBody ProductImageRequest request) {
        try {
            Product product = productService.addImage(id, request);
            return ResponseEntity.ok(productAssembler.toModel(product));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Buscar productos", description = "Busca productos según filtros opcionales: nombre, sku, categoría o marca")
    @ApiResponse(responseCode = "200", description = "Lista de productos que coinciden con los filtros")
    public ResponseEntity<CollectionModel<EntityModel<ProductResponse>>> searchProducts(
            @Parameter(description = "Nombre del producto") @RequestParam(required = false) String name,
            @Parameter(description = "SKU del producto") @RequestParam(required = false) String sku,
            @Parameter(description = "ID de la categoría") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "ID de la marca") @RequestParam(required = false) Long brandId) {

        List<EntityModel<ProductResponse>> products = productService.searchProducts(name, sku, categoryId, brandId)
                .stream()
                .map(productAssembler::toModel)
                .toList();

        return ResponseEntity.ok(CollectionModel.of(products));
    }
}
