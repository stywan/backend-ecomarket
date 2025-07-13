package com.ecomarket.backend.cart_order.client;

import com.ecomarket.backend.cart_order.client.dto.ProductDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/products")
public interface ProductClient {
    @GetExchange("/{id}")
    ProductDto getProduct(@PathVariable Long id);
}