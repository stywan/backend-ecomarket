package com.ecomarket.backend.cart_order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidReturnOperationException extends RuntimeException {
    public InvalidReturnOperationException(String message) {
        super(message);
    }
}