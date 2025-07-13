package com.ecomarket.backend.cart_order.client;

import com.ecomarket.backend.cart_order.client.dto.AddressDto;
import com.ecomarket.backend.cart_order.client.dto.UserDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/users")
public interface AuthClient {
    @GetExchange("/{id}")
    UserDto getUser(@PathVariable Long id);

    @GetExchange("/addresses/{id}")
    AddressDto getAddress(@PathVariable Long id);
}
